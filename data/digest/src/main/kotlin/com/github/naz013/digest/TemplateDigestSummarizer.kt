package com.github.naz013.digest

import android.content.Context
import com.github.naz013.ui.common.R
import org.threeten.bp.format.DateTimeFormatter

/**
 * Deterministic, zero-AI, zero-network sentence-building. Never returns `null` - this is the
 * same-run safety net [DigestSummarizerChain] falls back to when the on-device tier can't produce
 * a summary for this particular run (see "Why the template tier still exists" in
 * research/AI_DAILY_DIGEST_PLAN.md). It is never reachable on a device that failed the capability
 * gate in the first place.
 */
internal class TemplateDigestSummarizer(
  private val context: Context,
) : DigestSummarizer {
  override suspend fun summarize(input: DigestInput): String {
    val sentences = buildList {
      val firstReminder = input.reminders.firstOrNull()
      if (firstReminder != null) {
        val totalCount = input.reminders.size + input.overflowCount
        add(
          context.getString(
            R.string.ai_digest_template_reminders,
            totalCount,
            firstReminder.title,
            firstReminder.time.toLocalTime().format(TIME_FORMATTER),
          )
        )
      }
      if (input.birthdays.isNotEmpty()) {
        add(context.getString(R.string.ai_digest_template_birthdays, input.birthdays.joinToString()))
      }
    }
    return sentences.joinToString(separator = " ")
  }

  companion object {
    private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
  }
}
