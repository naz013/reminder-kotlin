package com.github.naz013.common.speech

sealed class SpeechError {
  data object AudioError : SpeechError()

  data object NoSpeechError : SpeechError()

  data class OperationError(
    val code: Int,
  ) : SpeechError()
}
