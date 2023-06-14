package com.elementary.tasks.core.os.power

import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import com.elementary.tasks.core.os.SystemServiceProvider
import timber.log.Timber
import kotlin.time.Duration

class WakeupManager(
  systemServiceProvider: SystemServiceProvider
) {

  private val powerManager = systemServiceProvider.providePowerManager()
  private val handler: Handler = Handler(Looper.getMainLooper())

  fun wakeUp(duration: Duration, tag: String) {
    Timber.d("trying to wake up for $duration")

    if (powerManager?.isInteractive == true) {
      Timber.d("screen is already on")
      return
    }

    if (powerManager?.isPowerSaveMode == true) {
      Timber.d("power save mode enabled")
      return
    }

    val wakeLock = try {
      powerManager?.newWakeLock(
        /* levelAndFlags = */ PowerManager.PARTIAL_WAKE_LOCK or
          PowerManager.ACQUIRE_CAUSES_WAKEUP or
          PowerManager.ON_AFTER_RELEASE,
        /* tag = */ tag
      )
    } catch (t: Throwable) {
      Timber.d(t, "failed to create wake lock")
      return
    }
    val isSuccess = try {
      wakeLock?.acquire(duration.inWholeMilliseconds)
      true
    } catch (t: Throwable) {
      Timber.e(t)
      false
    }
    Timber.d("wake lock acquired = $isSuccess")
  }
}
