package com.elementary.tasks.home.agenda

import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListState
import com.github.naz013.domain.Tag
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.logic.reminder.smartlist.SmartListFilter
import org.threeten.bp.LocalDateTime

data class AgendaScreenState(
  val listState: ListState = ListState.Loading,
  val hasAnyItems: Boolean = true,
  val searchQuery: String = "",
  val selectedCategories: Set<AgendaCategory> = AgendaCategory.entries.toSet(),
  val selectedSmartList: SmartListFilter? = null,
  val selectedTagId: String? = null,
  val selectedGroupId: String? = null,
  val availableTags: List<Tag> = emptyList(),
  val availableGroups: List<GroupV2> = emptyList(),
)

sealed interface ListState {
  data object Loading : ListState

  data class Ready(
    val items: List<UiAgendaItem>,
  ) : ListState

  data object Empty : ListState
}

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
) : UiAgendaItem
