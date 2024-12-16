package com.elementary.tasks.core.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.elementary.tasks.core.utils.Module
import com.github.naz013.logging.Logger

class SpeechEngine(
  private val context: Context,
  private val speechTextProcessor: SpeechTextProcessor = SpeechTextProcessor()
) {

  private var callback: SpeechEngineCallback? = null
  private var state = State.IDLE

  private var speech: SpeechRecognizer? = null
  private val listener = object : RecognitionListener {
    override fun onReadyForSpeech(bundle: Bundle?) {
      Logger.d("SpeechEngine:onReadyForSpeech")
      callback?.onStarted()
    }

    override fun onBeginningOfSpeech() {
      Logger.d("SpeechEngine:onBeginningOfSpeech")
      // Show a progress indicator
      callback?.onSpeechStarted()
      speechTextProcessor.saveSection()
    }

    override fun onRmsChanged(v: Float) {
    }

    override fun onBufferReceived(bytes: ByteArray?) {
    }

    override fun onEndOfSpeech() {
      Logger.d("SpeechEngine:onEndOfSpeech")
      // Hide the progress indicator
      callback?.onSpeechEnded()
    }

    override fun onError(i: Int) {
      Logger.d("SpeechEngine:onError error=$i")
      releaseSpeech()
      callback?.onSpeechError(SpeechError.NoSpeechError)
    }

    override fun onResults(bundle: Bundle?) {
    }

    override fun onPartialResults(bundle: Bundle?) {
      val results = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
      Logger.d("SpeechEngine:onPartialResults res=${results?.size}")
      if (results != null && results.size > 0) {
        callback?.onSpeechResult(speechTextProcessor.process(results[0].toString()))
      }
    }

    override fun onEvent(i: Int, bundle: Bundle?) {
      Logger.d("SpeechEngine:onEvent event=$i")
    }
  }

  fun setText(text: String) = speechTextProcessor.setText(text)

  fun supportsRecognition(): Boolean {
    return Module.hasMicrophone(context)
  }

  fun isStarted(): Boolean {
    return state == State.STARTED
  }

  fun startListening(callback: SpeechEngineCallback) {
    if (state == State.STARTED) {
      return
    }
    this.callback = callback
    try {
      if (speech != null) {
        releaseSpeech()
      }
      speech = SpeechRecognizer.createSpeechRecognizer(context)
      speech?.setRecognitionListener(listener)
      speech?.startListening(getIntent())
      state = State.STARTED
    } catch (e: Throwable) {
      Logger.e("SpeechEngine:startListening error=${e.message}")
      callback.onSpeechError(SpeechError.OperationError(0))
    }
  }

  fun stopListening() {
    if (state != State.STARTED) {
      return
    }
    callback?.onStopped()
    try {
      releaseSpeech()
    } catch (e: Throwable) {
      Logger.e("SpeechEngine:stopListening error=${e.message}")
      callback?.onSpeechError(SpeechError.OperationError(1))
    }
  }

  private fun releaseSpeech() {
    state = State.STOPPED
    callback?.onStopped()
    runCatching {
      speech?.stopListening()
      speech?.cancel()
      speech?.destroy()
    }
    speech = null
  }

  private fun getIntent(): Intent {
    val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    recognizerIntent.putExtra(
      RecognizerIntent.EXTRA_LANGUAGE_MODEL,
      RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
    )
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    recognizerIntent.putExtra(
      RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
      1000 * 5
    )
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      recognizerIntent.putExtra(
        RecognizerIntent.EXTRA_ENABLE_FORMATTING,
        RecognizerIntent.FORMATTING_OPTIMIZE_LATENCY
      )
    }
    return recognizerIntent
  }

  enum class State {
    IDLE,
    STARTED,
    STOPPED
  }
}
