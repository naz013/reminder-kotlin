package com.github.naz013.digest

import com.google.mlkit.genai.summarization.SummarizerOptions
import java.util.Locale

/**
 * ML Kit GenAI Summarization currently only supports English, Japanese, and Korean output (see
 * "What's new since the design doc" in research/AI_DAILY_DIGEST_PLAN.md). A hardware-capable
 * device set to any other language is treated the same as one without the required hardware -
 * digest capability is gated on both together, never hardware alone.
 */
internal enum class DigestLanguage(val mlKitLanguage: Int) {
  ENGLISH(SummarizerOptions.Language.ENGLISH),
  JAPANESE(SummarizerOptions.Language.JAPANESE),
  KOREAN(SummarizerOptions.Language.KOREAN),
}

internal fun currentDeviceDigestLanguage(): DigestLanguage? =
  when (Locale.getDefault().language) {
    "en" -> DigestLanguage.ENGLISH
    "ja" -> DigestLanguage.JAPANESE
    "ko" -> DigestLanguage.KOREAN
    else -> null
  }
