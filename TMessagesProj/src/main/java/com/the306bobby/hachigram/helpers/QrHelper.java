/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.helpers;

import android.graphics.Bitmap;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.TelegramQRCodeWriter;

import java.util.HashMap;

public class QrHelper {

    public static Bitmap createQR(String key) {
        try {
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 0);
            TelegramQRCodeWriter writer = new TelegramQRCodeWriter();
            return writer.encode(key, 768, 768, hints, null);
        } catch (Exception e) {
            FileLog.e(e);
        }
        return null;
    }
}
