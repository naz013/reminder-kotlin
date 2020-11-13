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
import timber.log.Timber
import java.io.File

class Sound(private val mContext: Context, private val prefs: Prefs) {
  private var mMediaPlayer: MediaPlayer? = null
  var isPaused: Boolean = false
    private set
  private var trimPlayback: Boolean = false
  private var playbackDuration: Int = 0
  private var playbackStartMillis: Long = 0

  private var lastFile: String? = null
  private var mRingtone: Ringtone? = null
  private var isDone: Boolean = false
  private var mCallback: PlaybackCallback? = null
  private val mRingtoneHandler = Handler()
  private val mRingtoneRunnable = object : Runnable {
    override fun run() {
      mRingtoneHandler.removeCallbacks(this)
      val ringtone = mRingtone
      if (ringtone != null && ringtone.isPlaying) {
        if (trimPlayback) {
          if (System.currentTimeMillis() - playbackStartMillis >= playbackDuration * 1000L) {
            stop(true)
          } else {
            mRingtoneHandler.postDelayed(this, 100)
          }
        } else {
          mRingtoneHandler.postDelayed(this, 100)
        }
      } else {
        stop(true)
      }
    }
  }

  private val mMelodyHandler = Handler()
  private val mMelodyRunnable = object : Runnable {
    override fun run() {
      mMelodyHandler.removeCallbacks(this)
      val mp = mMediaPlayer
      if (mp != null && mp.isPlaying) {
        if (trimPlayback) {
          if (System.currentTimeMillis() - playbackStartMillis >= playbackDuration * 1000L) {
            stop(true)
          } else {
            mMelodyHandler.postDelayed(this, 1000)
          }
        } else {
          mMelodyHandler.postDelayed(this, 1000)
        }
      } else {
        stop(true)
      }
    }
  }

  val isPlaying: Boolean
    get() {
      return try {
        val mp = mMediaPlayer
        val ringtone = mRingtone
        if (mp == null && ringtone == null) return false
        if (mp != null && mp.isPlaying) return true
        if (ringtone != null && ringtone.isPlaying) return true
        false
      } catch (e: Exception) {
        false
      }
    }

  fun setCallback(callback: PlaybackCallback?) {
    this.mCallback = callback
  }

  fun stop(notify: Boolean) {
    mRingtoneHandler.removeCallbacks(mRingtoneRunnable)
    mMelodyHandler.removeCallbacks(mMelodyRunnable)
    val mp = mMediaPlayer
    if (mp != null) {
      try {
        if (mp.isPlaying) {
          mp.stop()
          mp.release()
        }
      } catch (ignored: Exception) {
      }

      isPaused = false
    }
    val ringtone = mRingtone
    if (ringtone != null && ringtone.isPlaying) {
      try {
        ringtone.stop()
      } catch (ignored: Exception) {
      }
    }
    playbackStartMillis = 0L
    if (notify) {
      notifyFinish()
    }
  }

  fun pause() {
    val mp = mMediaPlayer ?: return
    try {
      mp.pause()
    } catch (ignored: Exception) {
    }
    isPaused = true
  }

  fun resume() {
    val mp = mMediaPlayer ?: return
    if (!isPaused) return
    try {
      mp.start()
    } catch (ignored: Exception) {
    }
    isPaused = false
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
      mMediaPlayer?.setDataSource(mContext, Uri.fromFile(file))
    } catch (e: Exception) {
      e.printStackTrace()
    }

    val attributes = AudioAttributes.Builder()
      .setLegacyStreamType(AudioManager.STREAM_MUSIC)
      .build()
    mMediaPlayer?.setAudioAttributes(attributes)
    mMediaPlayer?.isLooping = false
    mMediaPlayer?.setOnPreparedListener { mp ->
      notifyStart()
      mp.start()
    }
    mMediaPlayer?.setOnCompletionListener { notifyFinish() }
    mMediaPlayer?.setOnErrorListener { _, _, _ ->
      notifyFinish()
      false
    }
    try {
      mMediaPlayer?.prepareAsync()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun notifyFinish() {
    isDone = true
    mCallback?.onFinish()
  }

  fun playAlarm(path: Uri, looping: Boolean, duration: Int = 0) {
    if (isPlaying || !Permissions.checkPermission(mContext, Permissions.READ_EXTERNAL)) {
      return
    }
    stop(false)
    trimPlayback = duration > 0 && !looping
    playbackDuration = duration
    mMediaPlayer = MediaPlayer()
    try {
      mMediaPlayer?.setDataSource(mContext, path)
      var stream = AudioManager.STREAM_MUSIC
      if (prefs.isSystemLoudnessEnabled) {
        stream = prefs.soundStream
      }
      val attributes = AudioAttributes.Builder()
        .setLegacyStreamType(stream)
        .build()
      mMediaPlayer?.setAudioAttributes(attributes)
      mMediaPlayer?.isLooping = looping
      mMediaPlayer?.setOnPreparedListener { mp ->
        notifyStart()
        mp.start()
        playbackStartMillis = System.currentTimeMillis()
        mMelodyHandler.postDelayed(mMelodyRunnable, 1000)
      }
      mMediaPlayer?.setOnCompletionListener { notifyFinish() }
      mMediaPlayer?.setOnErrorListener { _, _, _ ->
        notifyFinish()
        false
      }
      mMediaPlayer?.prepareAsync()
    } catch (e: Exception) {
      playRingtone(path)
    }
  }

  fun playRingtone(path: Uri) {
    Timber.d("playRingtone: $path")
    notifyStart()
    mRingtone = RingtoneManager.getRingtone(mContext, path)
    mRingtone?.play()
    playbackStartMillis = System.currentTimeMillis()
    mRingtoneHandler.postDelayed(mRingtoneRunnable, 100)
  }

  fun playAlarm(afd: AssetFileDescriptor) {
    if (!Permissions.checkPermission(mContext, Permissions.READ_EXTERNAL)) return
    stop(false)
    if (isDone) {
      return
    }
    trimPlayback = false
    mMediaPlayer = MediaPlayer()
    try {
      mMediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    var stream = AudioManager.STREAM_MUSIC
    if (prefs.isSystemLoudnessEnabled) {
      stream = prefs.soundStream
    }
    val attributes = AudioAttributes.Builder()
      .setLegacyStreamType(stream)
      .build()
    mMediaPlayer?.setAudioAttributes(attributes)
    mMediaPlayer?.isLooping = false
    mMediaPlayer?.setOnPreparedListener { mp ->
      notifyStart()
      mp.start()
    }
    mMediaPlayer?.setOnCompletionListener { notifyFinish() }
    mMediaPlayer?.setOnErrorListener { _, _, _ ->
      notifyFinish()
      false
    }
    try {
      mMediaPlayer?.prepareAsync()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun notifyStart() {
    mCallback?.onStart()
  }

  interface PlaybackCallback {
    fun onStart()
    fun onFinish()
  }

  companion object {

    fun isDefaultMelody(defMelody: String): Boolean {
      return defMelody == Constants.SOUND_ALARM || defMelody == Constants.SOUND_NOTIFICATION
        || defMelody == Constants.SOUND_RINGTONE || defMelody == Constants.DEFAULT
    }
  }
}
