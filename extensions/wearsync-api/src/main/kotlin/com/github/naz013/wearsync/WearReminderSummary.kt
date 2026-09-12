package com.github.naz013.wearsync

/**
 * Glanceable projection of a [com.github.naz013.domain.reminder.v2.ReminderV2] sent to the Wear OS
 * companion app. Deliberately minimal - the watch only ever renders a title, a due time, a group
 * color and whether snoozing is offered, it never receives the full reminder.
 */
data class WearReminderSummary(
  val uuId: String,
  val title: String,
  val eventDateTimeMillis: Long?,
  val groupColor: Int?,
  val canSnooze: Boolean
)
