package com.elementary.tasks.core.speech

import com.github.naz013.logging.Logger

abstract class SpeechEngineCallback {
  open fun onStarted() {
    Logger.d("SpeechEngineCallback:onStarted")
  }

  open fun onStopped() {
    Logger.d("SpeechEngineCallback:onStopped")
  }

  open fun onSpeechStarted() {
    Logger.d("SpeechEngineCallback:onSpeechStarted")
  }

  open fun onSpeechEnded() {
    Logger.d("SpeechEngineCallback:onSpeechEnded")
  }

  open fun onSpeechError(error: SpeechError) {
    Logger.d("SpeechEngineCallback:onSpeechError error=$error")
  }

  open fun onSpeechResult(speechText: SpeechText) {
    Logger.d("SpeechEngineCallback:onSpeechResult text=$speechText")
  }
}
