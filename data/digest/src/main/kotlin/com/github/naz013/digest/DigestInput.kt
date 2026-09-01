package com.github.naz013.digest

import org.threeten.bp.LocalDateTime

/**
 * The only thing that ever reaches a summarizer (on-device or template) - titles, times, and
 * first names, never a reminder's notes/description field, never note content. Capped by
 * [DigestContentBuilder] before construction; [overflowCount] carries how many more reminders
 * were cut off, so summarizers can say "+N more" instead of silently dropping them.
 */
internal data class DigestInput(
  val reminders: List<DigestReminderItem>,
  val overflowCount: Int = 0,
  val birthdays: List<String> = emptyList(),
) {
  val isEmpty: Boolean get() = reminders.isEmpty() && overflowCount == 0 && birthdays.isEmpty()
}

internal data class DigestReminderItem(
  val title: String,
  val time: LocalDateTime,
)
