/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.core.helpers

import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.URLSpan
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.BuildVars
import org.telegram.messenger.FileLog
import org.telegram.messenger.LocaleController
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.ui.Components.URLSpanNoUnderline
import org.telegram.ui.LauncherIconController
import com.the306bobby.hachigram.core.configs.HachigramCoreConfig
import com.the306bobby.hachigram.core.configs.HachigramMessagesConfig
import com.the306bobby.hachigram.misc.Constants
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CGResourcesHelper {

    /** About app start */
    @JvmStatic
    fun getAppName(): String {
        if (HachigramCoreConfig.isStandaloneStableBuild() || HachigramCoreConfig.isPlayStoreBuild()) {
            return "Hachigram"
        } else if (HachigramCoreConfig.isStandaloneBetaBuild()) {
            return "Hachigram Beta"
        } else if (HachigramCoreConfig.isStandalonePremiumBuild()) {
            return "Hachigram Premium"
        } else if (HachigramCoreConfig.isDevBuild()) {
            return "Hachigram Dev"
        }
        return getString(R.string.CG_AppName)
    }

    @JvmStatic
    fun getBuildType(): String {
        if (HachigramCoreConfig.isStandaloneStableBuild()) {
            return getString(R.string.UP_BTRelease)
        } else if (HachigramCoreConfig.isPlayStoreBuild()) {
            return "Play Store"
        } else if (HachigramCoreConfig.isStandaloneBetaBuild()) {
            return getString(R.string.UP_BTBeta)
        } else if (HachigramCoreConfig.isStandalonePremiumBuild()) {
            return "Premium"
        } else if (HachigramCoreConfig.isDevBuild()) {
            return "Dev"
        }
        return "Unknown"
    }

    @JvmStatic
    fun getAbiCode(): String {
        var abi: String
        try {
            abi = Build.SUPPORTED_ABIS[0]
        } catch (e: Exception) {
            FileLog.e(e)
            abi = "universal"
        }
        return abi
    }

    @JvmStatic
    fun getCherryVersion() : String {
        return BuildConfig.BUILD_VERSION_STRING_CHERRY
    }

    @JvmStatic
    fun getSourceCodeVersion() : String {
        return BuildConfig.BUILD_SOURCE_CODE_VERSION
    }

    @JvmStatic
    fun getAboutString(): String {
        return getAppName() + " v" + getCherryVersion() + " (" + getAbiCode() + ")" +
                    "\n" +
                    "Based on Telegram v" + BuildVars.BUILD_VERSION_STRING + " (" + getSourceCodeVersion() + ")" +
                    "\n" +
                    Constants.CG_AUTHOR;
    }
    /** About app finish */

    /** Chats start */
    fun getLeftButtonText(noForwards: Boolean): String {
        if (noForwards) return getString(R.string.Reply)

        return when (HachigramMessagesConfig.leftBottomButton) {
            HachigramMessagesConfig.LEFT_BUTTON_REPLY -> getString(R.string.Reply)
            HachigramMessagesConfig.LEFT_BUTTON_SAVE_MESSAGE -> getString(R.string.CG_ToSaved)
            HachigramMessagesConfig.LEFT_BUTTON_DIRECT_SHARE -> getString(R.string.DirectShare)
            HachigramMessagesConfig.LEFT_BUTTON_FORWARD_WO_AUTHORSHIP -> capitalize(getString(R.string.CG_Without_Authorship))
            else -> capitalize(getString(R.string.CG_Without_Caption))
        }
    }

    fun getLeftButtonDrawable(noForwards: Boolean): Int {
        if (noForwards) return R.drawable.input_reply

        return when (HachigramMessagesConfig.leftBottomButton) {
            HachigramMessagesConfig.LEFT_BUTTON_REPLY -> R.drawable.input_reply
            HachigramMessagesConfig.LEFT_BUTTON_SAVE_MESSAGE -> R.drawable.msg_saved
            HachigramMessagesConfig.LEFT_BUTTON_DIRECT_SHARE -> R.drawable.msg_share
            else -> R.drawable.input_reply
        }
    }

    fun getReplyIconDrawable(): Int {
        return when (HachigramMessagesConfig.messageSlideAction) {
            HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_SAVE -> R.drawable.msg_saved_filled_solar
            HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_DIRECT_SHARE -> R.drawable.msg_share_filled
            HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_TRANSLATE -> R.drawable.msg_translate_filled_solar
            HachigramMessagesConfig.MESSAGE_SLIDE_ACTION_TRANSLATE_GEMINI -> R.drawable.msg_translate_filled_solar
            else -> R.drawable.filled_button_reply
        }
    }
    /** Chats finish */

    /** Profile activity start */
    fun getDCGeo(dcId: Int): String {
        return when (dcId) {
            1, 3 -> "USA (Miami)"
            2, 4 -> "NLD (Amsterdam)"
            5 -> "SGP (Singapore)"
            else -> "UNK (Unknown)"
        }
    }

    fun getDCName(dc: Int): String {
        return when (dc) {
            1 -> "Pluto"
            2 -> "Venus"
            3 -> "Aurora"
            4 -> "Vesta"
            5 -> "Flora"
            else -> getString(R.string.NumberUnknown)
        }
    }
    /** Profile activity finish */

    /** Misc start */
    fun getProperNotificationIcon(): Int { // App notification icon
        return if (HachigramCoreConfig.oldNotificationIcon) {
            R.drawable.notification
        } else {
            return if (isAnyOfBraIconsEnabled()) R.drawable.cg_notification_bra else R.drawable.cg_notification
        }
    }

    fun getResidentNotificationIcon(): Int {
        return if (HachigramCoreConfig.oldNotificationIcon) R.drawable.cg_notification else R.drawable.notification
    }

    fun isAnyOfBraIconsEnabled(): Boolean {
        return (LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.DARK_CHERRY_BRA)
                || LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.WHITE_CHERRY_BRA)
                || LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.VIOLET_SUNSET_CHERRY_BRA)
        )
    }

    fun createDateAndTime(date: Long): String {
        var dateAndTime = date
        try {
            dateAndTime *= 1000
            val rightNow = Calendar.getInstance()
            rightNow.timeInMillis = dateAndTime
            return String.format(
                $$"%1$s | %2$s", LocaleController.getInstance().formatterYear.format(
                Date(dateAndTime)
            ),
                LocaleController.getInstance().formatterDay.format(Date(dateAndTime))
            )
        } catch (_: Exception) { }
        return "LOC_ERR"
    }

    fun createDateAndTimeForJSON(date: Long): String {
        var dateAndTime = date
        try {
            dateAndTime *= 1000
            val rightNow = Calendar.getInstance()
            rightNow.timeInMillis = dateAndTime
            return String.format(
                $$"%1$s | %2$s", LocaleController.getInstance().formatterYear.format(
                Date(dateAndTime)
            ),
                LocaleController.getInstance().formatterDayWithSeconds.format(Date(dateAndTime))
            )
        } catch (_: Exception) { }
        return "LOC_ERR"
    }

    fun capitalize(text: String): String {
        var capitalizeString = ""
        if (text.trim() != "") {
            capitalizeString =
                text.take(1).uppercase(Locale.getDefault()) + text.substring(1)
        }
        return capitalizeString
    }

    fun getUrlNoUnderlineText(charSequence: CharSequence): CharSequence {
        val spannable: Spannable = SpannableString(charSequence)
        val spans = spannable.getSpans(0, charSequence.length, URLSpan::class.java)
        for (urlSpan in spans) {
            var span = urlSpan
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            spannable.removeSpan(span)
            span = object : URLSpanNoUnderline(span.url) {
            }
            spannable.setSpan(span, start, end, 0)
        }
        return spannable
    }
    /** Misc finish */

}