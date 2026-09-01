package com.github.naz013.digest

import android.content.Context
import com.github.naz013.digestapi.DigestCapabilityChecker
import com.github.naz013.logging.Logger
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizerOptions
import kotlinx.coroutines.guava.await

/**
 * Owns both the real ML Kit check and its own cache - see [DigestCapabilityChecker]'s doc for why.
 * Self-contained: this module's own tiny `SharedPreferences`, not `app`'s `Prefs`, keeps `digest`
 * free of any dependency on `app`.
 */
internal class DigestCapabilityCheckerImpl(
  private val context: Context,
) : DigestCapabilityChecker {

  private val cache by lazy {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  override fun isDeviceCapableCached(): Boolean = cache.getBoolean(KEY_CAPABLE, false)

  override suspend fun refreshCapability(): Boolean {
    val capable = checkCapability()
    cache.edit().putBoolean(KEY_CAPABLE, capable).apply()
    return capable
  }

  private suspend fun checkCapability(): Boolean {
    val language = currentDeviceDigestLanguage() ?: return false

    val options =
      SummarizerOptions.builder(context)
        .setInputType(SummarizerOptions.InputType.ARTICLE)
        .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
        .setLanguage(language.mlKitLanguage)
        .build()
    val summarizer = Summarization.getClient(options)
    return try {
      when (summarizer.checkFeatureStatus().await()) {
        FeatureStatus.UNAVAILABLE -> false
        else -> true // AVAILABLE, DOWNLOADABLE, DOWNLOADING all mean the hardware qualifies
      }
    } catch (e: GenAiException) {
      Logger.e(TAG, "Failed to check digest capability", e)
      false
    } finally {
      summarizer.close()
    }
  }

  companion object {
    private const val TAG = "DigestCapabilityChecker"
    private const val PREFS_NAME = "digest_capability_prefs"
    private const val KEY_CAPABLE = "device_capable"
  }
}
