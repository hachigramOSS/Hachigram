/**
 * This is the source code of Cherrygram Next for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.cherrygramnext.core.crashlytics

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.tgnet.TLRPC
import com.the306bobby.cherrygramnext.chats.helpers.ChatsHelper2
import com.the306bobby.cherrygramnext.core.configs.CherrygramCoreConfig
import com.the306bobby.cherrygramnext.core.configs.CherrygramDebugConfig
import com.the306bobby.cherrygramnext.core.configs.CherrygramPrivacyConfig
import com.the306bobby.cherrygramnext.core.helpers.CGResourcesHelper

object FirebaseAnalyticsHelper {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        if (!ApplicationLoader.checkPlayServices() || FirebaseApp.getApps(context).isEmpty()) return

        firebaseAnalytics = FirebaseAnalytics.getInstance(context).apply {
            val bundle = Bundle().apply {
                putString("flavor", CGResourcesHelper.getBuildType())
            }
            setDefaultEventParameters(bundle)

            setAnalyticsCollectionEnabled(CherrygramPrivacyConfig.googleAnalytics)
        }
    }

    fun onPrivacyConfigChanged(isEnabled: Boolean) {
        firebaseAnalytics?.setAnalyticsCollectionEnabled(isEnabled)

        if (CherrygramCoreConfig.isDevBuild()) {
            FileLog.e("Firebase Analytics collection: $isEnabled")
        }
    }

    fun trackEventWithEmptyBundle(eventName: String) {
        trackEvent(eventName, Bundle.EMPTY)
    }

    fun trackEvent(eventName: String, bundle: Bundle) {
        if (!CherrygramPrivacyConfig.googleAnalytics) return

        firebaseAnalytics?.let { analytics ->
            analytics.logEvent(eventName, bundle)

            if (CherrygramCoreConfig.isDevBuild()) {
                FileLog.e("отслежен ивент: $eventName $bundle")

                if (CherrygramDebugConfig.showRPCErrors) {
                    AndroidUtilities.runOnUIThread({
                        Toast.makeText(ApplicationLoader.applicationContext, eventName, Toast.LENGTH_SHORT).show()
                    }, 3000)
                }
            }
        }
    }

}