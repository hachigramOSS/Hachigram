package com.the306bobby.hachigram.helpers

import android.content.Context
import androidx.collection.LongSparseArray
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.MessageObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Remembers what each notification id was last posted with, so an unchanged
 * notification is not re-posted: on Android 16+ every re-post is a new heads-up.
 * Ported from inugram's NotificationsHelper (MIT).
 */
object NotificationsHelper {
    private val postedSignatures = ConcurrentHashMap<Int, HashMap<Int, String>>()
    private val prefs by lazy {
        ApplicationLoader.applicationContext.getSharedPreferences("hachigram_notifications", Context.MODE_PRIVATE)
    }

    private fun getWearIdsKey(account: Int) = "wear_ids_$account"

    // wearNotificationsIds is in-memory only while posted notifications outlive the
    // process; mirroring it to disk is what lets cancel paths survive a restart
    @JvmStatic
    fun loadWearNotificationIds(account: Int, into: LongSparseArray<Int>) {
        into.clear()
        val stored = prefs.getString(getWearIdsKey(account), null) ?: return
        for (entry in stored.splitToSequence(',')) {
            val separator = entry.indexOf(':')
            if (separator <= 0) continue
            val dialogId = entry.substring(0, separator).toLongOrNull() ?: continue
            val notificationId = entry.substring(separator + 1).toIntOrNull() ?: continue
            into.put(dialogId, notificationId)
        }
    }

    @JvmStatic
    fun saveWearNotificationIds(account: Int, ids: LongSparseArray<Int>) {
        val key = getWearIdsKey(account)
        val stored = (0 until ids.size()).joinToString(",") { "${ids.keyAt(it)}:${ids.valueAt(it)}" }
        if (prefs.getString(key, "") == stored) return
        // commit: this races a process death that may come right after posting the notifications
        prefs.edit().apply { if (stored.isEmpty()) remove(key) else putString(key, stored) }.commit()
    }

    @JvmStatic
    fun computeNotificationSignature(
        channelId: String?,
        name: String?,
        messages: List<MessageObject>?,
        storyCount: Int,
        maxId: Int,
        locked: Boolean,
        hasAvatar: Boolean,
    ): String = buildString {
        append(channelId).append('|').append(name).append('|').append(storyCount).append('|')
        append(maxId).append('|').append(locked).append('|').append(hasAvatar)
        messages?.forEach {
            append('|').append(it.id).append(':').append(it.messageOwner?.edit_date ?: 0)
        }
    }

    @JvmStatic
    fun shouldSkipNotify(account: Int, notificationId: Int, signature: String?): Boolean {
        if (signature == null) return false
        val map = postedSignatures.getOrPut(account) { HashMap() }
        if (map[notificationId] == signature) return true
        map[notificationId] = signature
        return false
    }

    @JvmStatic
    fun removePostedSignature(account: Int, notificationId: Int) {
        postedSignatures[account]?.remove(notificationId)
    }

    @JvmStatic
    fun clearPostedSignatures(account: Int) {
        postedSignatures[account]?.clear()
    }
}
