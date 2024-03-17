package com.elementary.tasks.reminder.lists.data

import com.elementary.tasks.core.data.ui.UiTextElement
import org.threeten.bp.LocalDateTime

sealed class UiReminderEventsList {
  abstract val id: String
}

data class UiReminderListHeader(
  val mainText: UiTextElement
) : UiReminderEventsList() {
  override val id: String = mainText.text
}

data class UiReminderList(
  override val id: String,
  val noteId: String?,
  val dueDateTime: LocalDateTime?,
  val mainText: UiTextElement,
  val secondaryText: UiTextElement?,
  val tertiaryText: UiTextElement?,
  val tags: List<UiTextElement>,
  val actions: UiReminderListActions,
  val state: UiReminderListState
) : UiReminderEventsList()

data class UiReminderListState(
  val isActive: Boolean = false,
  val isRemoved: Boolean = false,
  val isGps: Boolean = false
)

data class UiReminderListActions(
  val canToggle: Boolean = false,
  val canOpen: Boolean = false,
  val canEdit: Boolean = false,
  val canDelete: Boolean = false,
  val canSkip: Boolean = false
)
