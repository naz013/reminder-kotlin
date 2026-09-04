package com.github.naz013.ui.agenda

import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListState
import com.github.naz013.ui.common.text.UiTextElement
import org.threeten.bp.LocalDateTime

enum class AgendaCategory {
  REMINDERS,
  SHOPPING,
  LOCATION,
  BIRTHDAYS,
}

enum class AgendaMenuAction {
  OPEN,
  EDIT,
  DELETE,
  ARCHIVE,
  SKIP,
  TURN_OFF,
  PIN,
  UNPIN,
}

/** A single row (or header) in the merged, chronologically-sorted agenda list. */
sealed interface UiAgendaItem {
  val id: String
  val dateTime: LocalDateTime
}

data class UiAgendaHeader(
  override val id: String,
  override val dateTime: LocalDateTime,
  val text: String,
) : UiAgendaItem

data class UiAgendaReminder(
  override val id: String,
  override val dateTime: LocalDateTime,
  val category: AgendaCategory,
  val mainText: UiTextElement,
  val secondaryText: UiTextElement?,
  val tertiaryText: UiTextElement?,
  val tags: List<UiTextElement>,
  val actions: UiReminderListActions,
  val state: UiReminderListState,
  val isSelected: Boolean = false,
) : UiAgendaItem

data class UiAgendaBirthday(
  override val id: String,
  override val dateTime: LocalDateTime,
  val name: String,
  val ageFormatted: String,
  val remainingTimeFormatted: String?,
  val color: Int,
  val contrastColor: Int,
  val dateFormatted: String,
  val isSelected: Boolean = false,
) : UiAgendaItem

/** A read-only device/Google Calendar event, imported for display only - never a reminder. */
data class UiAgendaGoogleCalendarEvent(
  override val id: String,
  override val dateTime: LocalDateTime,
  val title: String,
  val calendarName: String,
  val allDay: Boolean,
) : UiAgendaItem
