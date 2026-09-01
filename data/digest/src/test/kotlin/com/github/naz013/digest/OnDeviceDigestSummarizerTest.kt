package com.github.naz013.digest

import android.content.Context
import com.google.common.util.concurrent.Futures
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationResult
import com.google.mlkit.genai.summarization.Summarizer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import java.util.Locale

class OnDeviceDigestSummarizerTest {
  private val context = mockk<Context>(relaxed = true)
  private val summarizer = mockk<Summarizer>(relaxed = true)
  private val onDeviceDigestSummarizer = OnDeviceDigestSummarizer(context)

  private val input = DigestInput(
    reminders = listOf(DigestReminderItem("Pay rent", LocalDateTime.of(2026, 3, 15, 9, 0))),
    birthdays = emptyList(),
  )

  @Before
  fun setUp() {
    Locale.setDefault(Locale.US)
    mockkStatic(Summarization::class)
    every { Summarization.getClient(any()) } returns summarizer
  }

  @After
  fun tearDown() {
    Locale.setDefault(Locale.US)
    unmockkStatic(Summarization::class)
  }

  @Test
  fun `returns null without calling ML Kit for an empty digest`() = runTest {
    val result = onDeviceDigestSummarizer.summarize(DigestInput(reminders = emptyList(), birthdays = emptyList()))

    assertNull(result)
  }

  @Test
  fun `returns null when the device language is unsupported`() = runTest {
    Locale.setDefault(Locale.FRANCE)

    val result = onDeviceDigestSummarizer.summarize(input)

    assertNull(result)
  }

  @Test
  fun `returns null when the feature is UNAVAILABLE`() = runTest {
    every { summarizer.checkFeatureStatus() } returns Futures.immediateFuture(FeatureStatus.UNAVAILABLE)

    val result = onDeviceDigestSummarizer.summarize(input)

    assertNull(result)
  }

  @Test
  fun `returns the summary when the feature is AVAILABLE`() = runTest {
    every { summarizer.checkFeatureStatus() } returns Futures.immediateFuture(FeatureStatus.AVAILABLE)
    val summarizationResult = mockk<SummarizationResult>()
    every { summarizationResult.summary } returns "AI generated summary"
    every { summarizer.runInference(any()) } returns Futures.immediateFuture(summarizationResult)

    val result = onDeviceDigestSummarizer.summarize(input)

    assertEquals("AI generated summary", result)
  }

  @Test
  fun `returns null when a DOWNLOADABLE model never finishes downloading within the timeout`() = runTest {
    every { summarizer.checkFeatureStatus() } returns Futures.immediateFuture(FeatureStatus.DOWNLOADABLE)
    // downloadFeature's callback is never invoked (relaxed mock, no-op) - awaitDownload hangs
    // until the bounded timeout fires, exercised here via runTest's virtual clock.

    val result = onDeviceDigestSummarizer.summarize(input)

    assertNull(result)
  }
}
