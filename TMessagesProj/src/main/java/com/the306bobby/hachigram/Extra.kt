/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram

import android.app.Activity
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.ChatObject
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.Bulletin
import org.telegram.ui.Components.BulletinFactory
import com.the306bobby.hachigram.core.helpers.CGResourcesHelper
import com.the306bobby.hachigram.helpers.UserHelper

object Extra {
    // https://core.telegram.org/api/obtaining_api_id
    @JvmField
    val APP_ID = BuildConfig.CG_APP_ID
    @JvmField
    val APP_HASH: String = BuildConfig.CG_APP_HASH

    // https://developers.google.com/identity/sms-retriever/verify#computing_your_apps_hash_string
    @JvmField
    val SMS_HASH: String = BuildConfig.CG_SMS_HASH

    const val ENDPOINT_FOR_DATE = "abcdefg"
    const val ENDPOINT_FOR_DATE_SECRET = "abcdefg"

    fun getRegistrationDate(fragment: BaseFragment, parentActivity: Activity, userID: Long, chatId: Long) {

        if (chatId != 0L) {
            val chat = fragment.messagesController.getChat(chatId)
            if (chat != null) {
                val date: CharSequence = if (ChatObject.isInChat(chat)) {
                    AndroidUtilities.replaceTags(
                        LocaleController.formatString(
                            R.string.CG_JoinedDate, chat.title,
                            LocaleController.formatDateTime(chat.date.toLong(), true)
                        )
                    )
                } else {
                    AndroidUtilities.replaceTags(
                        LocaleController.formatString(
                            R.string.CG_CreatedDate, chat.title,
                            LocaleController.formatDateTime(chat.date.toLong(), true)
                        )
                    )
                }

                BulletinFactory.of(fragment.getLayoutContainer(), fragment.resourceProvider)
                    .createSimpleBulletin(R.raw.chats_infotip, date)
                    .setDuration(Bulletin.DURATION_PROLONG)
                    .show()

                return
            }
        }

        val regDateFromTelegram = fragment.messagesController?.getPeerSettings(userID)?.registration_month

        if (regDateFromTelegram != null) {
            BulletinFactory.of(fragment.layoutContainer, fragment.resourceProvider)
                .createSimpleBulletin(R.raw.chats_infotip, UserHelper.getInstance(UserConfig.selectedAccount).getCreationDate(userID, true, regDateFromTelegram))
                .setDuration(Bulletin.DURATION_PROLONG)
                .show()
        } /*else {
            val progressDialog = AlertDialog(parentActivity, AlertDialog.ALERT_TYPE_SPINNER, fragment.resourceProvider)

            AndroidUtilities.runOnUIThread {
                try {
                    progressDialog.show()
                } catch (e: Exception) {
                    FileLog.e(e)
                }
            }

            UserHelper.getInstance(UserConfig.selectedAccount).getCreationDate(userID,
                {
                    BulletinFactory.of(fragment.layoutContainer, fragment.resourceProvider)
                        .createSimpleBulletin(R.raw.chats_infotip, getString(R.string.CG_RegistrationDateFailed))
                        .setDuration(Bulletin.DURATION_PROLONG)
                        .show()
                }
            ) {
                AndroidUtilities.runOnUIThread {
                    try {
                        if (progressDialog.isShowing) progressDialog.dismiss()
                    } catch (e: Exception) {
                        FileLog.e(e)
                    }
                }
                BulletinFactory.of(fragment.layoutContainer, fragment.resourceProvider)
                    .createSimpleBulletin(R.raw.chats_infotip, UserHelper.getInstance(UserConfig.selectedAccount).getCreationDate(userID, false, null))
                    .setDuration(Bulletin.DURATION_PROLONG)
                    .show()
            }
        }*/
    }

    fun addBirthdayToCalendar(parentActivity: Activity, userId: Long) {
        UserHelper.getInstance(UserConfig.selectedAccount).addBirthdayEvent(parentActivity, userId)
    }

    fun getProfileDC(user: TLRPC.User?, chat: TLRPC.Chat?): StringBuilder {
        val sb = StringBuilder()

        val dcId: Int = if (chat?.photo != null && chat.photo.dc_id > 0) {
            chat.photo.dc_id
        } else if (user != null) {
            user.photo?.dc_id?.takeIf { it > 0 }
                ?: run {
                    val userFull = MessagesController.getInstance(UserConfig.selectedAccount).getUserFull(user.id)
                    userFull?.profile_photo?.dc_id?.takeIf { it > 0 }
                        ?: userFull?.personal_photo?.dc_id?.takeIf { it > 0 }
                        ?: userFull?.fallback_photo?.dc_id?.takeIf { it > 0 }
                        ?: 0
                }
        } else {
            0
        }

        if (dcId > 0) {
            sb.append("DC: ")
                .append(dcId)
                .append(", ")
                .append(CGResourcesHelper.getDCName(dcId))
                .append(", ")
                .append(CGResourcesHelper.getDCGeo(dcId))
        } else {
            sb.append("DC: ").append(getString(R.string.NumberUnknown))
        }

        return sb
    }

}