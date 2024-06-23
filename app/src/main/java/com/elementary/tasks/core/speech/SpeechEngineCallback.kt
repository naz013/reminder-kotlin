package com.elementary.tasks.core.speech

import com.elementary.tasks.core.analytics.Traces

abstract class SpeechEngineCallback {
  open fun onStarted() {
    Traces.d("SpeechEngineCallback:onStarted")
  }

  open fun onStopped() {
    Traces.d("SpeechEngineCallback:onStopped")
  }

  open fun onSpeechStarted() {
    Traces.d("SpeechEngineCallback:onSpeechStarted")
  }

  open fun onSpeechEnded() {
    Traces.d("SpeechEngineCallback:onSpeechEnded")
  }

  open fun onSpeechError(error: SpeechError) {
    Traces.d("SpeechEngineCallback:onSpeechError error=$error")
  }

  open fun onSpeechResult(speechText: SpeechText) {
    Traces.d("SpeechEngineCallback:onSpeechResult text=$speechText")
  }
}
