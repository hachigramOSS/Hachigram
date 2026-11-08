package com.the306bobby.hachigram.helpers

import org.telegram.messenger.MessageObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Remembers what each notification id was last posted with, so an unchanged
 * notification is not re-posted: on Android 16+ every re-post is a new heads-up.
 * Ported from inugram's NotificationsHelper (MIT).
 */
object NotificationsHelper {
    private val postedSignatures = ConcurrentHashMap<Int, HashMap<Int, String>>()

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
