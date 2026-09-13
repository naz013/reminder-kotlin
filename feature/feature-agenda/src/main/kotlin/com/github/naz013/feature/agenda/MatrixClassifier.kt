package com.github.naz013.feature.agenda

import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.ui.agenda.UiAgendaReminder
import org.threeten.bp.LocalDate

/**
 * Groups reminders into Eisenhower quadrants using only data [UiAgendaReminder] already carries -
 * priority as the importance axis, and overdue/due-today as the urgency axis - so the Matrix view
 * needs no data beyond what the chronological Agenda list already resolves per reminder.
 */
internal fun classifyIntoQuadrants(
  reminders: List<UiAgendaReminder>,
  today: LocalDate = LocalDate.now(),
): Map<EisenhowerQuadrant, List<UiAgendaReminder>> =
  EisenhowerQuadrant.entries.associateWith { quadrant ->
    reminders.filter { quadrantOf(it, today) == quadrant }.sortedBy { it.dateTime }
  }

private fun quadrantOf(
  reminder: UiAgendaReminder,
  today: LocalDate,
): EisenhowerQuadrant {
  val important = reminder.priority == ReminderPriority.HIGH || reminder.priority == ReminderPriority.HIGHEST
  val urgent = reminder.isOverdue || reminder.dateTime.toLocalDate() == today
  return when {
    important && urgent -> EisenhowerQuadrant.DO_FIRST
    important && !urgent -> EisenhowerQuadrant.SCHEDULE
    !important && urgent -> EisenhowerQuadrant.DELEGATE
    else -> EisenhowerQuadrant.SOMEDAY
  }
}
