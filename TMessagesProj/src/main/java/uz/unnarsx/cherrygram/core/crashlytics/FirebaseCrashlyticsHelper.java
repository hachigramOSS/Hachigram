/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.crashlytics;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.telegram.messenger.ApplicationLoader;

public class FirebaseCrashlyticsHelper {

    public static boolean isAvailable() {
        return ApplicationLoader.checkPlayServices()
                && !FirebaseApp.getApps(ApplicationLoader.applicationContext).isEmpty();
    }

    // FirebaseCrashlytics.getInstance() throws when no google-services.json was
    // supplied at build time, and every caller is already inside a catch block.
    public static void recordException(Throwable e) {
        if (!isAvailable()) {
            return;
        }
        FirebaseCrashlytics.getInstance().recordException(e);
    }
}
