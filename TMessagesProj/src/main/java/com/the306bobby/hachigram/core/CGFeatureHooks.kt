/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.core

import com.the306bobby.hachigram.core.configs.HachigramChatsConfig

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGFeatureHooks {

    fun switchNoAuthor(b: Boolean) {
        HachigramChatsConfig.noAuthorship = b
    }

    fun switchNoCaptions(b: Boolean) {
        HachigramChatsConfig.noCaptions = b
    }

}