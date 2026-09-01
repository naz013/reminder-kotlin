package com.github.naz013.digest

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DigestSummarizerChainTest {
  private val onDeviceDigestSummarizer = mockk<OnDeviceDigestSummarizer>()
  private val templateDigestSummarizer = mockk<TemplateDigestSummarizer>()
  private val chain = DigestSummarizerChain(onDeviceDigestSummarizer, templateDigestSummarizer)

  private val input = DigestInput(reminders = emptyList(), birthdays = listOf("Alex"))

  @Test
  fun `returns the on-device result when it succeeds, never falling through to the template`() = runTest {
    coEvery { onDeviceDigestSummarizer.summarize(input) } returns "AI summary"

    val result = chain.summarize(input)

    assertEquals("AI summary", result)
    coVerify(exactly = 0) { templateDigestSummarizer.summarize(any()) }
  }

  @Test
  fun `falls through to the template when the on-device tier returns null`() = runTest {
    coEvery { onDeviceDigestSummarizer.summarize(input) } returns null
    coEvery { templateDigestSummarizer.summarize(input) } returns "Template summary"

    val result = chain.summarize(input)

    assertEquals("Template summary", result)
  }
}
