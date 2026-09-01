package com.github.naz013.digest

/** `null` return means "this run couldn't produce a summary" - see [DigestSummarizerChain]. */
internal interface DigestSummarizer {
  suspend fun summarize(input: DigestInput): String?
}
