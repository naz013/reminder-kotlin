package com.elementary.tasks.home.eventsview

import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.reminder.lists.data.UiReminderListActions
import com.elementary.tasks.reminder.lists.data.UiReminderListState
import com.github.naz013.usecase.reminders.smartlist.SmartListFilter
import org.threeten.bp.LocalDateTime

data class EventsScreenState(
  val listState: ListState = ListState.Loading,
  val searchQuery: String = "",
  val selectedCategories: Set<EventCategory> = EventCategory.entries.toSet(),
  val selectedSmartList: SmartListFilter? = null,
)

sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val items: List<UiEventItem>,
  ) : ListState

  data object Empty : ListState
}

enum class EventCategory {
  REMINDERS,
  SHOPPING,
  BIRTHDAYS,
}

enum class EventMenuAction {
  OPEN,
  EDIT,
  DELETE,
  ARCHIVE,
  SKIP,
  TURN_OFF,
}

/** A single row (or header) in the merged, chronologically-sorted events list. */
sealed interface UiEventItem {
  val id: String
  val dateTime: LocalDateTime
}

data class UiEventHeader(
  override val id: String,
  override val dateTime: LocalDateTime,
  val text: String,
) : UiEventItem

data class UiEventReminder(
  override val id: String,
  override val dateTime: LocalDateTime,
  val category: EventCategory,
  val mainText: UiTextElement,
  val secondaryText: UiTextElement?,
  val tertiaryText: UiTextElement?,
  val tags: List<UiTextElement>,
  val actions: UiReminderListActions,
  val state: UiReminderListState,
) : UiEventItem

data class UiEventBirthday(
  override val id: String,
  override val dateTime: LocalDateTime,
  val name: String,
  val ageFormatted: String,
  val remainingTimeFormatted: String?,
  val color: Int,
  val contrastColor: Int,
  val dateFormatted: String,
) : UiEventItem
