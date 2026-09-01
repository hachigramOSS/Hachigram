/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.preferences.helpers

object TextFieldAlert {

    fun removeNonNumericChars(input: String, allowMinus: Boolean): String {
        return if (allowMinus) {
            input.replace(Regex("[^0-9-]"), "")
        } else {
            input.replace(Regex("[^0-9]"), "")
        }
    }

}