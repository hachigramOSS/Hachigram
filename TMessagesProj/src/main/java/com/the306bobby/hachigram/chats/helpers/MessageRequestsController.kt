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
import kotlin.random.Random

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
        /** A request this account already auto-replied to, from any device. */
        private const val STATE_REQUEST_ANSWERED = 3

        /** Appended to every auto-reply so any device, with any custom text, can recognise one. */
        const val AUTO_REPLY_SIGNATURE = "- sent by hachigram"
        private const val HISTORY_LIMIT = 20

        @JvmStatic
        fun isAutoReply(message: TLRPC.Message?): Boolean {
            val text = message?.message ?: return false
            return message.out && text.trimEnd().endsWith(AUTO_REPLY_SIGNATURE)
        }

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

    fun isRequest(dialogId: Long): Boolean {
        val state = states[dialogId]
        return state == STATE_REQUEST || state == STATE_REQUEST_ANSWERED
    }

    /** MessagesController.isDialogMuted: a silenced request behaves like a muted chat. */
    fun isSilenced(dialogId: Long): Boolean =
        dialogId > 0 && HachigramPrivacyConfig.messageRequestsSilence && isEnabled() && isRequest(dialogId)

    fun requestsUnreadCount(): Int {
        var count = 0
        for (dialog in messagesController.getDialogs(FOLDER_ID)) {
            count += dialog.unread_count
        }
        return count
    }

    /** UI thread, from MessagesController.updateInterfaceWithMessages. */
    fun onMessages(dialogId: Long, messages: List<MessageObject>) {
        if (!isEnabled() || dialogId <= 0 || DialogObject.isEncryptedDialog(dialogId)) return
        // Outgoing messages reach every device of the account, so another phone's auto-reply or
        // real reply is the shared state; the text signature tells the two apart.
        var sawIncoming = false
        for (message in messages) {
            if (message.messageOwner is TLRPC.TL_messageService) continue
            if (!message.isOut) {
                sawIncoming = true
            } else {
                onOutgoingObserved(dialogId, message.messageOwner)
            }
        }
        if (!sawIncoming) return
        if (states.containsKey(dialogId) || pending.contains(dialogId)) return
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

    private fun onOutgoingObserved(dialogId: Long, message: TLRPC.Message) {
        val previous = states[dialogId]
        if (isAutoReply(message)) {
            if (previous == STATE_ACCEPTED || previous == STATE_REQUEST_ANSWERED) return
            remember(dialogId, STATE_REQUEST_ANSWERED)
            if (previous == null) refresh()
        } else {
            if (previous == STATE_ACCEPTED) return
            remember(dialogId, STATE_ACCEPTED)
            if (previous != null) refresh()
        }
    }

    private enum class HistoryVerdict { NO_OUTGOING, AUTO_REPLIED, ACCEPTED }

    // The chat history is the only state all devices share, so it has the last word on whether
    // we have written to them and whether another device already sent the auto-reply.
    private fun fetchHistoryVerdict(dialogId: Long, callback: (HistoryVerdict?) -> Unit) {
        val req = TLRPC.TL_messages_getHistory()
        req.peer = messagesController.getInputPeer(dialogId)
        req.limit = HISTORY_LIMIT
        connectionsManager.sendRequest(req) { response, _ ->
            AndroidUtilities.runOnUIThread {
                val res = response as? TLRPC.messages_Messages
                if (res == null) {
                    callback(null)
                    return@runOnUIThread
                }
                var verdict = HistoryVerdict.NO_OUTGOING
                for (message in res.messages) {
                    if (!message.out || message is TLRPC.TL_messageService) continue
                    if (isAutoReply(message)) {
                        verdict = HistoryVerdict.AUTO_REPLIED
                    } else {
                        verdict = HistoryVerdict.ACCEPTED
                        break
                    }
                }
                callback(verdict)
            }
        }
    }

    private fun checkServer(dialogId: Long) {
        fetchHistoryVerdict(dialogId) { verdict ->
            pending.remove(dialogId)
            if (verdict == null || states.containsKey(dialogId)) return@fetchHistoryVerdict
            when (verdict) {
                HistoryVerdict.ACCEPTED -> remember(dialogId, STATE_ACCEPTED)
                HistoryVerdict.AUTO_REPLIED -> {
                    remember(dialogId, STATE_REQUEST_ANSWERED)
                    onBecameRequest(dialogId)
                }
                HistoryVerdict.NO_OUTGOING -> {
                    remember(dialogId, STATE_REQUEST)
                    onBecameRequest(dialogId)
                    scheduleAutoReply(dialogId)
                }
            }
        }
    }

    private fun onBecameRequest(dialogId: Long) {
        refresh()
        if (HachigramPrivacyConfig.messageRequestsSilence) {
            // the message that created the request was already shown; take it back
            notificationsController.removeNotificationsForDialog(dialogId)
        }
    }

    // Two online devices see the same message within the same second. A random wait plus a fresh
    // look at the history before sending narrows the duplicate window to one round trip.
    private fun scheduleAutoReply(dialogId: Long) {
        if (!HachigramPrivacyConfig.messageRequestsAutoReply) return
        val text = HachigramPrivacyConfig.messageRequestsAutoReplyText.trim()
        if (text.isEmpty()) return
        AndroidUtilities.runOnUIThread({
            if (states[dialogId] != STATE_REQUEST) return@runOnUIThread
            fetchHistoryVerdict(dialogId) { verdict ->
                if (states[dialogId] != STATE_REQUEST) return@fetchHistoryVerdict
                when (verdict) {
                    HistoryVerdict.ACCEPTED -> {
                        remember(dialogId, STATE_ACCEPTED)
                        refresh()
                    }
                    HistoryVerdict.AUTO_REPLIED -> remember(dialogId, STATE_REQUEST_ANSWERED)
                    HistoryVerdict.NO_OUTGOING -> sendAutoReply(dialogId, text)
                    null -> {}
                }
            }
        }, 1000L + Random.nextLong(3000L))
    }

    private fun sendAutoReply(dialogId: Long, text: String) {
        val params = SendMessagesHelper.SendMessageParams.of(text + "\n\n" + AUTO_REPLY_SIGNATURE, dialogId, null, null, null, false, null, null, null, true, 0, 0, null, false)
        params.hachiAutoReply = true
        remember(dialogId, STATE_REQUEST_ANSWERED)
        sendMessagesHelper.sendMessage(params)
    }

    /** Anything we send ourselves accepts the request. From SendMessagesHelper. */
    fun onOutgoing(dialogId: Long) {
        if (dialogId <= 0 || DialogObject.isEncryptedDialog(dialogId)) return
        val previous = states[dialogId]
        if (previous == STATE_ACCEPTED) return
        remember(dialogId, STATE_ACCEPTED)
        if (previous != null) refresh()
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
            if (enabled && isRequest(dialog.id)) {
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
