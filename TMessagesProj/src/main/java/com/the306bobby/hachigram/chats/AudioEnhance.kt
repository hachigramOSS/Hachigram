/**
 * This is the source code of Hachigram for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.hachigram.chats

import android.media.MediaRecorder
import com.the306bobby.hachigram.core.configs.HachigramDebugConfig

object AudioEnhance {

    fun getAudioSource(): Int = when (HachigramDebugConfig.audioSource) {
        HachigramDebugConfig.AUDIO_SOURCE_CAMCORDER -> MediaRecorder.AudioSource.CAMCORDER
        HachigramDebugConfig.AUDIO_SOURCE_MIC -> MediaRecorder.AudioSource.MIC
        HachigramDebugConfig.AUDIO_SOURCE_REMOTE_SUBMIX -> MediaRecorder.AudioSource.REMOTE_SUBMIX
        HachigramDebugConfig.AUDIO_SOURCE_UNPROCESSED -> MediaRecorder.AudioSource.UNPROCESSED // Api 24
        HachigramDebugConfig.AUDIO_SOURCE_VOICE_CALL -> MediaRecorder.AudioSource.VOICE_CALL
        HachigramDebugConfig.AUDIO_SOURCE_VOICE_COMMUNICATION -> MediaRecorder.AudioSource.VOICE_COMMUNICATION
        HachigramDebugConfig.AUDIO_SOURCE_VOICE_DOWNLINK -> MediaRecorder.AudioSource.VOICE_DOWNLINK
        HachigramDebugConfig.AUDIO_SOURCE_VOICE_PERFORMANCE -> MediaRecorder.AudioSource.VOICE_PERFORMANCE // Api 29
        HachigramDebugConfig.AUDIO_SOURCE_VOICE_RECOGNITION -> MediaRecorder.AudioSource.VOICE_RECOGNITION
        HachigramDebugConfig.AUDIO_SOURCE_VOICE_UPLINK -> MediaRecorder.AudioSource.VOICE_UPLINK
        else -> MediaRecorder.AudioSource.DEFAULT
    }

}