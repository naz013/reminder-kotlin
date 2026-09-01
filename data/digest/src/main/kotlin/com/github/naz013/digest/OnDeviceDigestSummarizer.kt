package com.github.naz013.digest

import android.content.Context
import com.github.naz013.logging.Logger
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.Summarizer
import com.google.mlkit.genai.summarization.SummarizerOptions
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.threeten.bp.format.DateTimeFormatter
import kotlin.coroutines.resume

/**
 * Wraps ML Kit's GenAI Summarization API (Gemini Nano / AICore). Re-checks capability fresh on
 * every call rather than trusting [DigestCapabilityChecker]'s cache - this needs the granular
 * DOWNLOADABLE/DOWNLOADING states the cache doesn't carry, to decide whether to wait out a model
 * download for this one run. Coordinates/API surface are beta as of this writing - see
 * "What's new since the design doc" in research/AI_DAILY_DIGEST_PLAN.md.
 */
internal class OnDeviceDigestSummarizer(
  private val context: Context,
) : DigestSummarizer {

  override suspend fun summarize(input: DigestInput): String? {
    val language = currentDeviceDigestLanguage()
    if (input.isEmpty || language == null) return null
    return runSummarization(input, language)
  }

  private suspend fun runSummarization(input: DigestInput, language: DigestLanguage): String? {
    val options =
      SummarizerOptions.builder(context)
        .setInputType(SummarizerOptions.InputType.ARTICLE)
        .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
        .setLanguage(language.mlKitLanguage)
        .build()
    val summarizer = Summarization.getClient(options)

    return try {
      if (ensureModelReady(summarizer)) {
        val request = SummarizationRequest.builder(buildPrompt(input)).build()
        summarizer.runInference(request).await().summary
      } else {
        null
      }
    } catch (e: GenAiException) {
      Logger.e(TAG, "On-device digest summarization failed", e)
      null
    } finally {
      summarizer.close()
    }
  }

  private suspend fun ensureModelReady(summarizer: Summarizer): Boolean =
    when (summarizer.checkFeatureStatus().await()) {
      FeatureStatus.AVAILABLE -> true
      FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING ->
        withTimeoutOrNull(DOWNLOAD_TIMEOUT_MS) { awaitDownload(summarizer) } ?: false
      else -> false
    }

  private suspend fun awaitDownload(summarizer: Summarizer): Boolean =
    suspendCancellableCoroutine { continuation ->
      summarizer.downloadFeature(
        object : DownloadCallback {
          override fun onDownloadStarted(bytesToDownload: Long) = Unit

          override fun onDownloadProgress(totalBytesDownloaded: Long) = Unit

          override fun onDownloadCompleted() {
            if (continuation.isActive) continuation.resume(true)
          }

          override fun onDownloadFailed(e: GenAiException) {
            Logger.e(TAG, "Digest model download failed", e)
            if (continuation.isActive) continuation.resume(false)
          }
        },
      )
    }

  /**
   * Plain-English prose regardless of [language] - the summarizer is asked to output in
   * [language], but per-input-language prompt phrasing (ja/ko) is deferred until real device
   * testing on those locales is possible, see the design doc's short-input-length risk note.
   */
  private fun buildPrompt(input: DigestInput): String =
    buildString {
      if (input.reminders.isNotEmpty()) {
        append("Reminders today: ")
        append(
          input.reminders.joinToString { "${it.title} (${it.time.toLocalTime().format(TIME_FORMATTER)})" },
        )
        if (input.overflowCount > 0) append(", and ${input.overflowCount} more")
        append(". ")
      }
      if (input.birthdays.isNotEmpty()) {
        append("Today's birthdays: ")
        append(input.birthdays.joinToString())
        append(".")
      }
    }

  companion object {
    private const val TAG = "OnDeviceDigestSummarizer"
    private const val DOWNLOAD_TIMEOUT_MS = 15_000L
    private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
  }
}
