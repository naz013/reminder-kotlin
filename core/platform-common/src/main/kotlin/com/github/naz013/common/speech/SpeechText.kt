package com.github.naz013.common.speech

data class SpeechText(
  val text: String,
  val newText: NewText?,
)

data class NewText(
  val text: String,
  val startIndex: Int,
  val endIndex: Int,
)
