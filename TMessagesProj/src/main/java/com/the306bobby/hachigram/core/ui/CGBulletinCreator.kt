/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.core.ui

import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ContactsController
import org.telegram.messenger.LocaleController.formatString
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.Bulletin
import org.telegram.ui.Components.BulletinFactory
import com.the306bobby.hachigram.core.helpers.AppRestartHelper

object CGBulletinCreator {

    fun createRestartBulletin(fragment: BaseFragment) {
        BulletinFactory.of(fragment).createSimpleBulletin(
            R.raw.chats_infotip,
            getString(R.string.CG_RestartToApply),
            getString(R.string.BotUnblock)
        ) {
            AppRestartHelper.restartApp(fragment.context)
        }.show()
    }

    fun createDebugSuccessBulletin(fragment: BaseFragment) {
        BulletinFactory.of(fragment)
            .createSuccessBulletin(getString(R.string.OK))
            .setDuration(Bulletin.DURATION_LONG)
            .show()
    }

    fun createSwitchAccountBulletin(account: Int) {
        val nextAcc: TLObject? = UserConfig.getInstance(account).currentUser

        if (nextAcc is TLRPC.User) {
            AndroidUtilities.runOnUIThread({
                val accs = ArrayList<TLObject?>()
                accs.add(nextAcc)

                val text: CharSequence = AndroidUtilities.replaceTags(
                    formatString(
                        R.string.CG_SwitchedToAccount,
                        ContactsController.formatName(nextAcc.first_name, nextAcc.last_name)
                    )
                )

                BulletinFactory.global().createChatsBulletin(accs, text, null)
                    .setDuration(Bulletin.DURATION_LONG)
                    .show()

                accs.clear()
            }, 200)
        }
    }

}
