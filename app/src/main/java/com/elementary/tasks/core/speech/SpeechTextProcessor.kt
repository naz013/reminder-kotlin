package com.elementary.tasks.core.speech

import com.elementary.tasks.core.analytics.Traces

class SpeechTextProcessor(initValue: String = "") {

  private var completedSection = initValue
  private var currentSection: String = initValue
  private var lastSpeechText: SpeechText? = null

  fun setText(text: String) {
    Traces.d("SpeechTextProcessor:setText text=$text")
    completedSection = if (text.isEmpty()) {
      ""
    } else {
      "$text "
    }
  }

  fun saveSection() {
    if (currentSection.isEmpty()) {
      return
    }
    completedSection = "$completedSection$currentSection"
    currentSection = ""
    lastSpeechText = null
  }

  fun process(text: String): SpeechText {
    if (text.isEmpty() && currentSection.isEmpty()) {
      Traces.d("SpeechTextProcessor:process text is empty and currentSection is empty")
      return SpeechText(text = completedSection, newText = null)
    }
    if (text.isEmpty()) {
      Traces.d("SpeechTextProcessor:process text is empty")
      saveSection()
      return SpeechText(text = completedSection, newText = null)
    }

    val startIndex = if (currentSection.isEmpty()) {
      completedSection.length
    } else {
      completedSection.length + currentSection.length
    }
    val result = if (completedSection.isEmpty()) {
      text
    } else {
      "$completedSection $text"
    }
    val newText = if (currentSection.isEmpty()) {
      text
    } else {
      if (currentSection.length < text.length) {
        text.substring(currentSection.length)
      } else {
        text
      }
    }
    val sameAsLast = text == currentSection
    currentSection = text

    val newSpeechText = SpeechText(
      text = result,
      newText = NewText(newText, startIndex, startIndex + newText.length)
    )
    return if (sameAsLast) {
      lastSpeechText ?: newSpeechText.also {
        lastSpeechText = it
      }
    } else {
      newSpeechText.also {
        lastSpeechText = it
      }
    }
  }
}
