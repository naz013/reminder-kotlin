package com.github.naz013.ui.notification.settings

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Plays a vibration pattern immediately, e.g. as feedback when the user picks a preset in
 *  [VibrationPresets.ALL] - so they can feel the difference rather than just read a name. */
class VibrationPlayer(
  private val context: Context,
) {
  fun play(pattern: List<Long>) {
    if (pattern.isEmpty()) return
    val vibrator = vibrator() ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createWaveform(pattern.toLongArray(), -1))
    } else {
      @Suppress("DEPRECATION")
      vibrator.vibrate(pattern.toLongArray(), -1)
    }
  }

  private fun vibrator(): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}
