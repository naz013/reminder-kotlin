package com.elementary.tasks.core.utils

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler

import java.io.File
import java.io.IOException

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class Sound(private val mContext: Context) {
    private var mMediaPlayer: MediaPlayer? = null
    var isPaused: Boolean = false
        private set
    private var lastFile: String? = null
    private var mRingtone: Ringtone? = null
    private var isDone: Boolean = false
    private var mCallback: PlaybackCallback? = null
    private val mRingtoneHandler = Handler()
    private val mRingtoneRunnable = object : Runnable {
        override fun run() {
            mRingtoneHandler.removeCallbacks(this)
            if (mRingtone != null && mRingtone!!.isPlaying) {
                mRingtoneHandler.postDelayed(this, 100)
            } else {
                mRingtone = null
                notifyFinish()
            }
        }
    }

    val isPlaying: Boolean
        get() {
            return try {
                mMediaPlayer != null && mMediaPlayer!!.isPlaying
            } catch (e: IllegalStateException) {
                false
            }

        }

    fun setCallback(callback: PlaybackCallback?) {
        this.mCallback = callback
    }

    fun stop(notify: Boolean) {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer!!.stop()
                mMediaPlayer!!.release()
            } catch (ignored: IllegalStateException) {
            }

            isPaused = false
        }
        if (mRingtone != null) {
            mRingtoneHandler.removeCallbacks(mRingtoneRunnable)
            try {
                mRingtone!!.stop()
            } catch (ignored: Exception) {
            }

        }
        if (notify) notifyFinish()
    }

    fun pause() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer!!.pause()
            } catch (ignored: IllegalStateException) {
            }

            isPaused = true
        }
    }

    fun resume() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer!!.start()
            } catch (ignored: IllegalStateException) {
            }

            isPaused = false
        }
    }

    fun isSameFile(path: String): Boolean {
        return lastFile != null && path.equals(lastFile!!, ignoreCase = true)
    }

    fun play(path: String) {
        if (!Permissions.checkPermission(mContext, Permissions.READ_EXTERNAL)) return
        lastFile = path
        stop(false)
        mMediaPlayer = MediaPlayer()
        try {
            val file = File(path)
            mMediaPlayer!!.setDataSource(mContext, Uri.fromFile(file))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (Module.isLollipop) {
            val attributes = AudioAttributes.Builder()
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build()
            mMediaPlayer!!.setAudioAttributes(attributes)
        } else {
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
        mMediaPlayer!!.isLooping = false
        mMediaPlayer!!.setOnPreparedListener { mp ->
            notifyStart()
            mp.start()
        }
        mMediaPlayer!!.setOnCompletionListener { notifyFinish() }
        mMediaPlayer!!.setOnErrorListener { _, _, _ ->
            notifyFinish()
            false
        }
        try {
            mMediaPlayer!!.prepareAsync()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    private fun notifyFinish() {
        isDone = true
        if (mCallback != null) {
            mCallback!!.onFinish()
        }
    }

    fun playAlarm(path: Uri, looping: Boolean) {
        if (isPlaying && !Permissions.checkPermission(mContext, Permissions.READ_EXTERNAL)) {
            return
        }
        stop(false)
        mMediaPlayer = MediaPlayer()
        try {
            mMediaPlayer!!.setDataSource(mContext, path)
            val prefs = Prefs.getInstance(mContext)
            var stream = AudioManager.STREAM_MUSIC
            if (prefs.isSystemLoudnessEnabled) {
                stream = prefs.soundStream
            }
            if (Module.isLollipop) {
                val attributes = AudioAttributes.Builder()
                        .setLegacyStreamType(stream)
                        .build()
                mMediaPlayer!!.setAudioAttributes(attributes)
            } else {
                mMediaPlayer!!.setAudioStreamType(stream)
            }
            mMediaPlayer!!.isLooping = looping
            mMediaPlayer!!.setOnPreparedListener { mp ->
                notifyStart()
                mp.start()
            }
            mMediaPlayer!!.setOnCompletionListener { notifyFinish() }
            mMediaPlayer!!.setOnErrorListener { _, _, _ ->
                notifyFinish()
                false
            }
            try {
                mMediaPlayer!!.prepareAsync()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        } catch (e: IOException) {
            playRingtone(path)
        }

    }

    private fun playRingtone(path: Uri) {
        notifyStart()
        mRingtone = RingtoneManager.getRingtone(mContext, path)
        mRingtone!!.play()
        mRingtoneHandler.postDelayed(mRingtoneRunnable, 100)
    }

    fun playAlarm(afd: AssetFileDescriptor) {
        if (!Permissions.checkPermission(mContext, Permissions.READ_EXTERNAL)) return
        stop(false)
        if (isDone) {
            return
        }
        mMediaPlayer = MediaPlayer()
        try {
            mMediaPlayer!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val prefs = Prefs.getInstance(mContext)
        var stream = AudioManager.STREAM_MUSIC
        if (prefs.isSystemLoudnessEnabled) {
            stream = prefs.soundStream
        }
        if (Module.isLollipop) {
            val attributes = AudioAttributes.Builder()
                    .setLegacyStreamType(stream)
                    .build()
            mMediaPlayer!!.setAudioAttributes(attributes)
        } else {
            mMediaPlayer!!.setAudioStreamType(stream)
        }
        mMediaPlayer!!.isLooping = false
        mMediaPlayer!!.setOnPreparedListener { mp ->
            notifyStart()
            mp.start()
        }
        mMediaPlayer!!.setOnCompletionListener { notifyFinish() }
        mMediaPlayer!!.setOnErrorListener { _, _, _ ->
            notifyFinish()
            false
        }
        try {
            mMediaPlayer!!.prepareAsync()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    private fun notifyStart() {
        if (mCallback != null) {
            mCallback!!.onStart()
        }
    }

    interface PlaybackCallback {
        fun onStart()
        fun onFinish()
    }

    companion object {

        fun isDefaultMelody(defMelody: String): Boolean {
            return defMelody == Constants.DEFAULT
        }
    }
}
