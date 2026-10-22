package com.github.naz013.feature.routine.run

import android.media.AudioManager
import android.media.ToneGenerator

internal interface StepTonePlayer {
  fun play()

  fun release()
}

internal class ToneGeneratorStepTonePlayer : StepTonePlayer {
  private var toneGenerator: ToneGenerator? = null

  override fun play() {
    runCatching {
      if (toneGenerator == null) {
        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, TONE_VOLUME)
      }
      toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, TONE_DURATION_MS)
    }
  }

  override fun release() {
    toneGenerator?.release()
    toneGenerator = null
  }

  private companion object {
    private const val TONE_VOLUME = 80
    private const val TONE_DURATION_MS = 150
  }
}
