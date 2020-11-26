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
import android.os.Looper
import timber.log.Timber
import java.io.File

class Sound(
  private val context: Context,
  private val prefs: Prefs
) {

  private var mediaPlayer: MediaPlayer? = null
  var isPaused: Boolean = false
    private set
  private var trimPlayback: Boolean = false
  private var playbackDuration: Int = 0
  private var playbackStartMillis: Long = 0

  private var lastFile: String? = null
  private var mRingtone: Ringtone? = null
  private var isDone: Boolean = false
  private var playbackCallback: PlaybackCallback? = null
  private val ringtoneHandler = Handler(Looper.getMainLooper())
  private val ringtoneRunnable = object : Runnable {
    override fun run() {
      ringtoneHandler.removeCallbacks(this)
      val ringtone = mRingtone
      if (ringtone != null && ringtone.isPlaying) {
        if (trimPlayback) {
          if (System.currentTimeMillis() - playbackStartMillis >= playbackDuration * 1000L) {
            stop(true)
          } else {
            ringtoneHandler.postDelayed(this, 100)
          }
        } else {
          ringtoneHandler.postDelayed(this, 100)
        }
      } else {
        stop(true)
      }
    }
  }

  private val melodyHandler = Handler(Looper.getMainLooper())
  private val melodyRunnable = object : Runnable {
    override fun run() {
      melodyHandler.removeCallbacks(this)
      val mp = mediaPlayer
      if (mp != null && mp.isPlaying) {
        if (trimPlayback) {
          if (System.currentTimeMillis() - playbackStartMillis >= playbackDuration * 1000L) {
            stop(true)
          } else {
            melodyHandler.postDelayed(this, 1000)
          }
        } else {
          melodyHandler.postDelayed(this, 1000)
        }
      } else {
        stop(true)
      }
    }
  }

  val isPlaying: Boolean
    get() {
      return try {
        val mp = mediaPlayer
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
    this.playbackCallback = callback
  }

  fun stop(notify: Boolean) {
    ringtoneHandler.removeCallbacks(ringtoneRunnable)
    melodyHandler.removeCallbacks(melodyRunnable)
    val mp = mediaPlayer
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
    val mp = mediaPlayer ?: return
    try {
      mp.pause()
    } catch (ignored: Exception) {
    }
    isPaused = true
  }

  fun resume() {
    val mp = mediaPlayer ?: return
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
    if (!Permissions.checkPermission(context, Permissions.READ_EXTERNAL)) return
    lastFile = path
    stop(false)
    mediaPlayer = MediaPlayer()
    try {
      val file = File(path)
      mediaPlayer?.setDataSource(context, Uri.fromFile(file))
    } catch (e: Exception) {
      e.printStackTrace()
    }

    val attributes = AudioAttributes.Builder()
      .setLegacyStreamType(AudioManager.STREAM_MUSIC)
      .build()
    mediaPlayer?.setAudioAttributes(attributes)
    mediaPlayer?.isLooping = false
    mediaPlayer?.setOnPreparedListener { mp ->
      notifyStart()
      mp.start()
    }
    mediaPlayer?.setOnCompletionListener { notifyFinish() }
    mediaPlayer?.setOnErrorListener { _, _, _ ->
      notifyFinish()
      false
    }
    try {
      mediaPlayer?.prepareAsync()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun notifyFinish() {
    isDone = true
    playbackCallback?.onFinish()
  }

  fun playAlarm(path: Uri, looping: Boolean, duration: Int = 0) {
    if (isPlaying || !Permissions.checkPermission(context, Permissions.READ_EXTERNAL)) {
      return
    }
    stop(false)
    trimPlayback = duration > 0 && !looping
    playbackDuration = duration
    mediaPlayer = MediaPlayer()
    try {
      mediaPlayer?.setDataSource(context, path)
      var stream = AudioManager.STREAM_MUSIC
      if (prefs.isSystemLoudnessEnabled) {
        stream = prefs.soundStream
      }
      val attributes = AudioAttributes.Builder()
        .setLegacyStreamType(stream)
        .build()
      mediaPlayer?.setAudioAttributes(attributes)
      mediaPlayer?.isLooping = looping
      mediaPlayer?.setOnPreparedListener { mp ->
        notifyStart()
        mp.start()
        playbackStartMillis = System.currentTimeMillis()
        melodyHandler.postDelayed(melodyRunnable, 1000)
      }
      mediaPlayer?.setOnCompletionListener { notifyFinish() }
      mediaPlayer?.setOnErrorListener { _, _, _ ->
        notifyFinish()
        false
      }
      mediaPlayer?.prepareAsync()
    } catch (e: Exception) {
      playRingtone(path)
    }
  }

  fun playRingtone(path: Uri) {
    Timber.d("playRingtone: $path")
    notifyStart()
    mRingtone = RingtoneManager.getRingtone(context, path)
    mRingtone?.play()
    playbackStartMillis = System.currentTimeMillis()
    ringtoneHandler.postDelayed(ringtoneRunnable, 100)
  }

  fun playAlarm(afd: AssetFileDescriptor) {
    if (!Permissions.checkPermission(context, Permissions.READ_EXTERNAL)) return
    stop(false)
    if (isDone) {
      return
    }
    trimPlayback = false
    mediaPlayer = MediaPlayer()
    try {
      mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
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
    mediaPlayer?.setAudioAttributes(attributes)
    mediaPlayer?.isLooping = false
    mediaPlayer?.setOnPreparedListener { mp ->
      notifyStart()
      mp.start()
    }
    mediaPlayer?.setOnCompletionListener { notifyFinish() }
    mediaPlayer?.setOnErrorListener { _, _, _ ->
      notifyFinish()
      false
    }
    try {
      mediaPlayer?.prepareAsync()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun notifyStart() {
    playbackCallback?.onStart()
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
