/**
 * This is the source code of Cherrygram Next for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.cherrygramnext.core.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.telegram.messenger.MediaController
import com.the306bobby.cherrygramnext.core.configs.CherrygramCoreConfig

class SleepHelper : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!MediaController.getInstance().isMessagePaused) {
            MediaController.getInstance().pauseMessage(MediaController.getInstance().playingMessageObject)
        }
        CherrygramCoreConfig.sleepTimer = false
    }

}