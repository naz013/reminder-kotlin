package com.github.naz013.digest

/** Tries the on-device tier first, falls through to the deterministic template - always succeeds. */
internal class DigestSummarizerChain(
  private val onDeviceDigestSummarizer: OnDeviceDigestSummarizer,
  private val templateDigestSummarizer: TemplateDigestSummarizer,
) {
  suspend fun summarize(input: DigestInput): String =
    onDeviceDigestSummarizer.summarize(input) ?: templateDigestSummarizer.summarize(input)
}
