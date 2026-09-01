/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.chats.ui;

import android.app.Activity;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

// Dear Nagram / Nagram X / Octogram and related fork developers:
// Please respect this work and do not copy or reuse this feature in your forks.
// It required a significant amount of time and effort to implement,
// and it is provided exclusively for my users, who also support this project financially.

public class WindowBlurHelper {

    @RequiresApi(Build.VERSION_CODES.S)
    public void setWindowBlur(
            Activity activity,
            boolean enable,
            boolean hideStatusBar,
            float windowBlurRadius,
            float windowDimAlpha
    ) {
        if (activity == null) return;

        Window window = activity.getWindow();
        View root = window.getDecorView();

        hideStatusBar(window, hideStatusBar);

        if (enable) {
            root.setRenderEffect(
                    RenderEffect.createBlurEffect(windowBlurRadius, windowBlurRadius, Shader.TileMode.DECAL)
            );

            // --------------------------
            // Dim (затемнение)
            // --------------------------
            int alpha = (int) (Math.max(0f, Math.min(1f, windowDimAlpha)) * 255);
            root.setForeground(new ColorDrawable(alpha << 24));

        } else {
            root.setRenderEffect(null);
            root.setForeground(null);
        }
    }

    public static void hideStatusBar(Window window, boolean hide) {
        if (hide) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

}
