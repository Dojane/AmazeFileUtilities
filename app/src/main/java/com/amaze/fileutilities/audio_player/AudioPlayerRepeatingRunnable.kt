/*
 * Copyright (C) 2021-2021 Team Amaze - Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com>. All Rights reserved.
 *
 * This file is part of Amaze File Utilities.
 *
 * 'Amaze File Utilities' is a registered trademark of Team Amaze. All other product
 * and company names mentioned are trademarks or registered trademarks of their respective owners.
 */

package com.amaze.fileutilities.audio_player

import com.amaze.fileutilities.utilis.AbstractRepeatingRunnable
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class AudioPlayerRepeatingRunnable(
    startImmediately: Boolean,
    private val serviceRef: WeakReference<OnPlayerRepeatingCallback>
) :
    AbstractRepeatingRunnable(
        1, 1, TimeUnit.SECONDS,
        startImmediately
    ) {

    private var lastPosition = 0L

    override fun run() {
        if (serviceRef.get() == null) {
            cancel()
            return
        }
        val callback = serviceRef.get()
        callback?.let {
            it.getAudioProgressHandlerCallback()?.let {
                audioProgressHandler ->
                if (audioProgressHandler.isCancelled) {
                    it.onProgressUpdate(audioProgressHandler)
                    cancel()
                    return
                }
                val audioPlaybackInfo = audioProgressHandler.audioPlaybackInfo
                audioPlaybackInfo.currentPosition = it.getPlayerPosition()
                audioPlaybackInfo.duration = it.getPlayerDuration()
                audioPlaybackInfo.isPlaying = it.isPlaying()
                if (lastPosition != audioPlaybackInfo.currentPosition) {
                    it.onProgressUpdate(audioProgressHandler)
                }
                lastPosition = it.getPlayerPosition()
            }
        }
    }
}

interface OnPlayerRepeatingCallback {
    fun getAudioProgressHandlerCallback(): AudioProgressHandler?
    fun onProgressUpdate(audioProgressHandler: AudioProgressHandler)
    fun getPlayerPosition(): Long
    fun getPlayerDuration(): Long
    fun isPlaying(): Boolean
}
