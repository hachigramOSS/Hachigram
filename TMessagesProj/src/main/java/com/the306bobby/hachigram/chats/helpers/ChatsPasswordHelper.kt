/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.chats.helpers

import android.os.Build
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.telegram.messenger.BaseController
import org.telegram.messenger.DialogObject
import org.telegram.messenger.FileLog
import org.telegram.messenger.FingerprintController
import org.telegram.messenger.MessageObject
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC.MessageEntity
import org.telegram.tgnet.TLRPC.TL_messageEntitySpoiler
import com.the306bobby.hachigram.core.CGBiometricPrompt
import com.the306bobby.hachigram.core.configs.HachigramCoreConfig
import com.the306bobby.hachigram.core.configs.HachigramPrivacyConfig

class ChatsPasswordHelper private constructor(num: Int) : BaseController(num) {

    companion object {
        private val instances = arrayOfNulls<ChatsPasswordHelper>(UserConfig.MAX_ACCOUNT_COUNT)

        @JvmStatic
        fun getInstance(num: Int): ChatsPasswordHelper {
            return instances[num] ?: synchronized(ChatsPasswordHelper::class.java) {
                instances[num] ?: ChatsPasswordHelper(num).also { instances[num] = it }
            }
        }
    }

    private var lockedChatsCache: HashSet<String>? = null

    fun getPasscodeArray(): String = "locked_chats_list"

    fun saveArrayList(list: ArrayList<String>, key: String) {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил saveArrayList")

        if (key == getPasscodeArray()) {
            lockedChatsCache = HashSet(list)
        }

        messagesController.mainSettings
            .edit {
                putString(key, Gson().toJson(list))
            }
    }

    fun getArrayList(key: String): ArrayList<String> {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил кешированный getArrayList для паролей")

        if (key == getPasscodeArray() && lockedChatsCache != null) {
            return ArrayList(lockedChatsCache!!)
        }

        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил getArrayList для паролей")

        val json = messagesController.mainSettings.getString(key, null)
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("getArrayList: $json")
        val list: ArrayList<String> = Gson().fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
            ?: arrayListOf(userConfig.clientUserId.toString())

        if (key == getPasscodeArray()) {
            lockedChatsCache = HashSet(list)
        }

        return list
    }

    fun isChatLocked(chatId: Long): Boolean {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил isChatLocked")
        if (chatId == 0L || !HachigramPrivacyConfig.askBiometricsToOpenChat) return false

        if (lockedChatsCache == null) {
            getArrayList(getPasscodeArray())
        }

        val idStr = chatId.toString()
        return lockedChatsCache?.contains(idStr) == true || lockedChatsCache?.contains("-$idStr") == true
    }

    fun isChatLocked(messageObject: MessageObject): Boolean {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил isChatLocked2")
        return HachigramPrivacyConfig.askBiometricsToOpenChat && messageObject.messageOwner.message != null
                && !messageObject.isStoryReactionPush && !messageObject.isStoryPush
                && !messageObject.isStoryMentionPush && !messageObject.isStoryPushHidden
                && isChatLocked(messageObject.chatId)
    }

    fun isEncryptedChat(chatId: Long): Boolean {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил isEncryptedChat")
        if (HachigramPrivacyConfig.askBiometricsToOpenEncrypted) {
            val encID = DialogObject.getEncryptedChatId(chatId)
            val encryptedChat = messagesController.getEncryptedChat(encID)
            return encryptedChat != null
        } else {
            return false
        }
    }

    fun isEncryptedChat(messageObject: MessageObject): Boolean {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил isEncryptedChat2")
        if (HachigramPrivacyConfig.askBiometricsToOpenEncrypted) {
            val encID = DialogObject.getEncryptedChatId(messageObject.dialogId)
            val encryptedChat = messagesController.getEncryptedChat(encID)
            return messageObject.messageOwner.message != null
                    && !messageObject.isStoryReactionPush && !messageObject.isStoryPush
                    && !messageObject.isStoryMentionPush && !messageObject.isStoryPushHidden
                    && encryptedChat != null
        } else {
            return false
        }
    }

    fun checkLockedChatsEntities(messageObject: MessageObject): ArrayList<MessageEntity>? {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил checkLockedChatsEntities")
        return checkLockedChatsEntities(messageObject, messageObject.messageOwner.entities)
    }

    fun checkLockedChatsEntities(messageObject: MessageObject, original: ArrayList<MessageEntity>?): ArrayList<MessageEntity>? {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил checkLockedChatsEntities2")
        return if (isChatLocked(messageObject) || isEncryptedChat(messageObject)) {
            val entities = original?.let { ArrayList(it) }
            val spoiler = TL_messageEntitySpoiler()
            spoiler.offset = 0
            spoiler.length = messageObject.messageOwner.message.length
            entities?.add(spoiler)
            entities
        } else {
            original
        }
    }

    private var spoilerChars: CharArray = charArrayOf(
        '⠌', '⡢', '⢑', '⠨', '⠥', '⠮', '⡑'
    )

    fun replaceStringToSpoilers(originalText: String?, force: Boolean): String? {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил replaceStringToSpoilers")
        if (originalText == null) {
            return null
        }
        return if (HachigramPrivacyConfig.askBiometricsToOpenArchive || force) {
            val stringBuilder = StringBuilder(originalText)
            for (i in originalText.indices) {
                stringBuilder.setCharAt(i, spoilerChars[i % spoilerChars.size])
            }
            stringBuilder.toString()
        } else {
            originalText
        }
    }

    fun getLockedChatsCount(): Int {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил getLockedChatsCount")
        return getArrayList(getPasscodeArray()).size
    }

    fun shouldRequireBiometrics(userID: Long, chatID: Long, encID: Long): Boolean {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил shouldRequireBiometrics")
        val lockedChat = (userID != 0L && isChatLocked(userID)) || (chatID != 0L && isChatLocked(chatID))

        val encryptedChat = encID != 0L && isEncryptedChat(encID)

        return (lockedChat && shouldRequireBiometricsToOpenChats()) || (encryptedChat && shouldRequireBiometricsToOpenEncryptedChats())
    }

    fun shouldRequireBiometricsToOpenChats(): Boolean {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил shouldRequireBiometricsToOpenChats")
        return HachigramPrivacyConfig.askBiometricsToOpenChat && checkBiometricAvailable()
    }

    fun shouldRequireBiometricsToOpenEncryptedChats(): Boolean {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил shouldRequireBiometricsToOpenEncryptedChats")
        return HachigramPrivacyConfig.askBiometricsToOpenEncrypted && checkBiometricAvailable()
    }

    fun askPasscodeBeforeDelete(): Boolean {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил askPasscodeBeforeDelete")
        return HachigramPrivacyConfig.askPasscodeBeforeDelete && checkBiometricAvailable()
    }

    fun checkBiometricAvailable(): Boolean {
        if (HachigramCoreConfig.isDevBuild()) FileLog.d("запросил checkBiometricAvailable")

        val hasBiometrics = CGBiometricPrompt.hasBiometricEnrolled()
        if (!hasBiometrics) return false

        val hasFingerprints = CGBiometricPrompt.hasEnrolledFingerprints()
        return if (hasFingerprints) {
            FingerprintController.isKeyReady() && !FingerprintController.checkDeviceFingerprintsChanged()
        } else {
            true
        }
    }

}