/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.misc

import com.the306bobby.hachigram.core.configs.HachigramCoreConfig

object Constants {

    @JvmField
    var CG_AUTHOR = "Updates: @hachigramReleases"

    /** CG Links start**/
    @JvmField
    var CG_CHANNEL_USERNAME = "hachigramReleases"
    @JvmField
    var CG_CHANNEL_URL = "https://t.me/hachigramReleases"

    @JvmField
    var CG_APKS_CHANNEL_USERNAME = "hachigramReleases"
    @JvmField
    var CG_APKS_CHANNEL_URL = "https://t.me/hachigramReleases"

    @JvmField
    var CG_CHAT_USERNAME = "hachigramSupport"
    @JvmField
    var CG_CHAT_URL = "https://t.me/hachigramSupport"

    @JvmField
    var UPDATE_APP_URL = if (HachigramCoreConfig.isPlayStoreBuild()) "https://play.google.com/store/apps/details?id=com.the306bobby.hachigram" else CG_CHANNEL_URL

    @JvmField
    var CG_PRIVACY_URL = "https://the306bobby.com/hachigram/privacy"

    @JvmField
    var CG_CROWDIN_URL = "https://crowdin.com/project/hachigram"
    @JvmField
    var CG_GITHUB_URL = "https://github.com/hachigramOSS/Hachigram"
    /** CG Links finish**/

    const val PACKAGE_NAME = "com.the306bobby.hachigram"

    /** CG Chats IDs start**/
    const val Hachigram_Channel = 4356320113L // t.me/hachigramReleases
    const val Hachigram_Support = 4422734048L // t.me/hachigramSupport
    /** CG Chats IDs finish**/

    /** Misc start**/
    const val CHERRY_EMOJI_ID = 5220045200780458122L // Hachigram logo
    const val CHERRY_EMOJI_ID_BRA = 5222458839256825177L // Hachigram logo (bra)
    const val CHERRY_EMOJI_ID_VERIFIED = 5449476181864779205L // Hachigram Verified adaptive logo
    const val CHERRY_EMOJI_ID_VERIFIED_BRA = 5451850156318181341L // Hachigram Verified Bra adaptive logo
    const val CHERRY_EMOJI_ID_DONATE = 5411229175971322671L // Cherry emoji with eyeglasses
    const val CHERRY_EMOJI_ID_PREMIUM = 5393391313502609448L // Cherry emoji with stars
    const val CHERRY_EMOJI_ID_PREMIUM_MOON = 5370777017904011118L // Evil moon emoji
    const val PROFILE_BACKGROUND_COLOR_ID_GREEN_BLUE = 12 // Blue-Green gradient
    const val PROFILE_BACKGROUND_COLOR_ID_RED = 14 // Red-Pink gradient
    const val REPLY_BACKGROUND_COLOR_ID = 13 // Red-Pink gradient
    /** Misc finish**/

    /** Firebase remote Config start */
    const val Videomessages_Resolution = "videomessages_resolution"
    const val Re_Tg_Check = "re_tg_check"
    const val is_new_updates_ui_available = "is_new_updates_ui_available"
    const val is_new_updates_ui_available_v2 = "is_new_updates_ui_available_v2"
    /** Firebase remote Config finish */

}