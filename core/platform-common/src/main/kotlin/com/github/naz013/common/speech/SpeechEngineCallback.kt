package com.github.naz013.common.speech

import com.github.naz013.logging.Logger

abstract class SpeechEngineCallback {
  open fun onStarted() {
    Logger.d(TAG, "onStarted")
  }

  open fun onStopped() {
    Logger.d(TAG, "onStopped")
  }

  open fun onSpeechStarted() {
    Logger.d(TAG, "onSpeechStarted")
  }

  open fun onSpeechEnded() {
    Logger.d(TAG, "onSpeechEnded")
  }

  open fun onSpeechError(error: SpeechError) {
    Logger.d(TAG, "onSpeechError error=$error")
  }

  open fun onSpeechResult(speechText: SpeechText) {
    Logger.d(TAG, "onSpeechResult text=$speechText")
  }

  companion object {
    private const val TAG = "SpeechEngineCallback"
  }
}
