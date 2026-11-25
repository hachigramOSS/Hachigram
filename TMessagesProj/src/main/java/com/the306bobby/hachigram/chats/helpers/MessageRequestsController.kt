/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.chats.helpers

import androidx.core.content.edit
import com.the306bobby.hachigram.core.configs.HachigramPrivacyConfig
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.BaseController
import org.telegram.messenger.DialogObject
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessageObject
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.messenger.SendMessagesHelper
import org.telegram.messenger.UserConfig
import org.telegram.messenger.UserObject
import org.telegram.tgnet.TLRPC

/**
 * Keeps first messages from people we never wrote to in a local "message requests" folder.
 *
 * The folder is client-side only: request dialogs get [FOLDER_ID] as their folder_id every time
 * dialogs are sorted, so whatever the server says about folder 0/1 is overridden until we reply.
 */
class MessageRequestsController private constructor(num: Int) : BaseController(num) {

    companion object {
        /** 0 is the chat list and 1 is Telegram's archive; the server never hands out 2. */
        const val FOLDER_ID = 2

        private const val PREF_KEY = "hachi_message_requests"
        private const val STATE_REQUEST = 1
        private const val STATE_ACCEPTED = 2

        private val instances = arrayOfNulls<MessageRequestsController>(UserConfig.MAX_ACCOUNT_COUNT)

        @JvmStatic
        fun getInstance(num: Int): MessageRequestsController {
            return instances[num] ?: synchronized(MessageRequestsController::class.java) {
                instances[num] ?: MessageRequestsController(num).also { instances[num] = it }
            }
        }

        @JvmStatic
        fun isEnabled(): Boolean = HachigramPrivacyConfig.messageRequestsFolder
    }

    private val states = HashMap<Long, Int>()
    private val pending = HashSet<Long>()

    init {
        load()
    }

    private fun load() {
        val raw = messagesController.mainSettings.getString(PREF_KEY, null) ?: return
        for (entry in raw.split(',')) {
            val sep = entry.indexOf(':')
            if (sep <= 0) continue
            val id = entry.substring(0, sep).toLongOrNull() ?: continue
            val state = entry.substring(sep + 1).toIntOrNull() ?: continue
            states[id] = state
        }
    }

    private fun save() {
        val raw = states.entries.joinToString(",") { "${it.key}:${it.value}" }
        messagesController.mainSettings.edit { putString(PREF_KEY, raw) }
    }

    fun isRequest(dialogId: Long): Boolean = states[dialogId] == STATE_REQUEST

    fun requestsUnreadCount(): Int {
        var count = 0
        for (dialog in messagesController.getDialogs(FOLDER_ID)) {
            count += dialog.unread_count
        }
        return count
    }

    /** UI thread, from MessagesController.updateInterfaceWithMessages. */
    fun onIncomingMessages(dialogId: Long, messages: List<MessageObject>) {
        if (!isEnabled() || dialogId <= 0 || DialogObject.isEncryptedDialog(dialogId)) return
        if (states.containsKey(dialogId) || pending.contains(dialogId)) return
        if (messages.none { !it.isOut && it.messageOwner !is TLRPC.TL_messageService }) return
        val user = messagesController.getUser(dialogId) ?: return
        if (!isUnsolicitedSender(user)) return
        if (hasReadSomethingOfOurs(dialogId)) {
            remember(dialogId, STATE_ACCEPTED)
            return
        }
        pending.add(dialogId)
        hasLocalOutgoing(dialogId) { hasOutgoing ->
            if (hasOutgoing) {
                pending.remove(dialogId)
                remember(dialogId, STATE_ACCEPTED)
            } else {
                checkServer(dialogId)
            }
        }
    }

    private fun isUnsolicitedSender(user: TLRPC.User): Boolean {
        return !user.bot && !user.self && !user.support && !user.deleted &&
            !UserObject.isContact(user) && !UserObject.isService(user.id) && !UserObject.isReplyUser(user.id) &&
            user.id != UserObject.VERIFY && user.id != UserObject.ANONYMOUS
    }

    // read_outbox_max_id is the newest message of ours they have read: anything above 0 means we wrote first
    private fun hasReadSomethingOfOurs(dialogId: Long): Boolean {
        val cached = messagesController.dialogs_read_outbox_max[dialogId] ?: 0
        val dialog = messagesController.getDialog(dialogId)
        return cached > 0 || (dialog != null && dialog.read_outbox_max_id > 0)
    }

    // Covers a message we sent that they have not read yet, which the server-side counter misses
    private fun hasLocalOutgoing(dialogId: Long, callback: (Boolean) -> Unit) {
        messagesStorage.storageQueue.postRunnable {
            var found = false
            try {
                val cursor = messagesStorage.database.queryFinalized("SELECT mid FROM messages_v2 WHERE uid = $dialogId AND out = 1 LIMIT 1")
                found = cursor.next()
                cursor.dispose()
            } catch (e: Exception) {
                FileLog.e(e)
            }
            AndroidUtilities.runOnUIThread { callback(found) }
        }
    }

    private fun checkServer(dialogId: Long) {
        val req = TLRPC.TL_messages_getPeerDialogs()
        val peer = TLRPC.TL_inputDialogPeer()
        peer.peer = messagesController.getInputPeer(dialogId)
        req.peers.add(peer)
        connectionsManager.sendRequest(req) { response, _ ->
            AndroidUtilities.runOnUIThread {
                pending.remove(dialogId)
                val res = response as? TLRPC.TL_messages_peerDialogs ?: return@runOnUIThread
                val dialog = res.dialogs.firstOrNull() ?: return@runOnUIThread
                if (dialog.read_outbox_max_id > 0) {
                    remember(dialogId, STATE_ACCEPTED)
                    return@runOnUIThread
                }
                if (states.containsKey(dialogId)) return@runOnUIThread
                remember(dialogId, STATE_REQUEST)
                refresh()
                sendAutoReply(dialogId)
            }
        }
    }

    private fun sendAutoReply(dialogId: Long) {
        if (!HachigramPrivacyConfig.messageRequestsAutoReply) return
        val text = HachigramPrivacyConfig.messageRequestsAutoReplyText.trim()
        if (text.isEmpty()) return
        val params = SendMessagesHelper.SendMessageParams.of(text, dialogId, null, null, null, false, null, null, null, true, 0, 0, null, false)
        params.hachiAutoReply = true
        sendMessagesHelper.sendMessage(params)
    }

    /** Anything we send ourselves accepts the request. From SendMessagesHelper. */
    fun onOutgoing(dialogId: Long) {
        if (dialogId <= 0 || DialogObject.isEncryptedDialog(dialogId)) return
        val previous = states[dialogId]
        if (previous == STATE_ACCEPTED) return
        remember(dialogId, STATE_ACCEPTED)
        if (previous == STATE_REQUEST) refresh()
    }

    fun onDialogDeleted(dialogId: Long) {
        if (states.remove(dialogId) != null) save()
    }

    private fun remember(dialogId: Long, state: Int) {
        states[dialogId] = state
        save()
    }

    fun refresh() {
        messagesController.sortDialogs(null)
        notificationCenter.postNotificationName(NotificationCenter.dialogsNeedReload, true)
    }

    /** Top of MessagesController.sortDialogs: assigns the local folder and keeps its row in step. */
    fun applyToDialogs() {
        val mc = messagesController
        val enabled = isEnabled()
        var any = false
        for (dialog in mc.allDialogs) {
            if (dialog is TLRPC.TL_dialogFolder) continue
            if (enabled && states[dialog.id] == STATE_REQUEST) {
                dialog.folder_id = FOLDER_ID
                any = true
            } else if (dialog.folder_id == FOLDER_ID) {
                dialog.folder_id = 0
            }
        }
        val folderDialogId = DialogObject.makeFolderDialogId(FOLDER_ID)
        if (any) {
            val folder = mc.ensureFolderDialogExists(FOLDER_ID, null) ?: return
            folder.folder.title = LocaleController.getString(R.string.SP_MessageRequests_Folder)
            // The archive row has to stay first: the chat list assumes the hidden archive is at position 0
            val archive = mc.dialogs_dict.get(DialogObject.makeFolderDialogId(1))
            if (archive != null && archive.pinnedNum <= folder.pinnedNum) {
                folder.pinnedNum = archive.pinnedNum - 1
            }
        } else {
            val folder = mc.dialogs_dict.get(folderDialogId) ?: return
            mc.dialogs_dict.remove(folderDialogId)
            mc.allDialogs.remove(folder)
            AndroidUtilities.runOnUIThread { notificationCenter.postNotificationName(NotificationCenter.folderBecomeEmpty, FOLDER_ID) }
        }
    }
}
