/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.core.configs

import android.app.Activity
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import com.the306bobby.hachigram.preferences.boolean
import com.the306bobby.hachigram.preferences.string

object HachigramPrivacyConfig {

    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    /** Privacy start */
    var hideProxySponsor by sharedPreferences.boolean("SP_NoProxyPromo", true)
    /** Privacy finish */

    /** Passcode lock start */
    var hideArchiveFromChatsList by sharedPreferences.boolean("SP_HideArchiveFromChatsList", false)
    var askBiometricsToOpenArchive by sharedPreferences.boolean("SP_AskBiometricsToOpenArchive", false)
    var askBiometricsToOpenEncrypted by sharedPreferences.boolean("SP_AskBiometricsToOpenEncrypted", false)
    var askBiometricsToOpenChat by sharedPreferences.boolean("SP_AskBiometricsToOpenChat", false)
    var askPasscodeBeforeDelete by sharedPreferences.boolean("SP_AskPinBeforeDelete", false)
    var allowSystemPasscode by sharedPreferences.boolean("SP_AllowSystemPasscode", false)
    /** Passcode lock finish */

    /** Message requests start */
    var messageRequestsFolder by sharedPreferences.boolean("SP_MessageRequestsFolder", false)
    var messageRequestsAutoReply by sharedPreferences.boolean("SP_MessageRequestsAutoReply", false)
    var messageRequestsSilence by sharedPreferences.boolean("SP_MessageRequestsSilence", false)
    var messageRequestsAutoReplyText by sharedPreferences.string("SP_MessageRequestsAutoReplyText", "Hey! I do not take unsolicited PMs, and will likely not see this unless I am expecting it. Please ask to PM in the group you found me in if not already done so :)")
    /** Message requests finish */

    /** Misc **/
    var hideArchivedStories by sharedPreferences.boolean("CP_HideArchivedStories", false)
    var reTgCheck by sharedPreferences.boolean("SP_ReTgCheck", true)
    /** Misc **/

}