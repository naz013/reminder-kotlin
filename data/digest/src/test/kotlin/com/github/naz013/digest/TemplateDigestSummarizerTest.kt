package com.github.naz013.digest

import android.content.Context
import com.github.naz013.ui.common.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class TemplateDigestSummarizerTest {
  private val context = mockk<Context>(relaxed = true)
  private lateinit var summarizer: TemplateDigestSummarizer

  @Before
  fun setUp() {
    summarizer = TemplateDigestSummarizer(context)
    every { context.getString(R.string.ai_digest_template_reminders, any(), any(), any()) } returns "reminders sentence"
    every { context.getString(R.string.ai_digest_template_birthdays, any()) } returns "birthdays sentence"
  }

  @Test
  fun `never returns an empty result untouched - returns blank for a fully empty input`() = runTest {
    val result = summarizer.summarize(DigestInput(reminders = emptyList(), birthdays = emptyList()))

    assertEquals("", result)
  }

  @Test
  fun `builds the reminders sentence from the total count including overflow and the first item`() = runTest {
    val input = DigestInput(
      reminders = listOf(DigestReminderItem("Pay rent", LocalDateTime.of(2026, 3, 15, 9, 0))),
      overflowCount = 2,
      birthdays = emptyList(),
    )

    val result = summarizer.summarize(input)

    verify { context.getString(R.string.ai_digest_template_reminders, 3, "Pay rent", "9:00") }
    assertEquals("reminders sentence", result)
  }

  @Test
  fun `appends the birthdays sentence when birthdays are present`() = runTest {
    val input = DigestInput(
      reminders = emptyList(),
      birthdays = listOf("Alex", "Sam"),
    )

    val result = summarizer.summarize(input)

    verify { context.getString(R.string.ai_digest_template_birthdays, "Alex, Sam") }
    assertEquals("birthdays sentence", result)
  }

  @Test
  fun `combines both sentences when reminders and birthdays are both present`() = runTest {
    val input = DigestInput(
      reminders = listOf(DigestReminderItem("Pay rent", LocalDateTime.of(2026, 3, 15, 9, 0))),
      birthdays = listOf("Alex"),
    )

    val result = summarizer.summarize(input)

    assertEquals("reminders sentence birthdays sentence", result)
  }
}
