package com.github.naz013.ui.reminder

import com.github.naz013.ui.common.text.UiTextElement
import org.threeten.bp.LocalDateTime

sealed class UiReminderEventsList {
  abstract val id: String
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
  val state: UiReminderListState,
  val isSelected: Boolean = false,
) : UiReminderEventsList()

data class UiReminderListState(
  val isActive: Boolean = false,
  val isRemoved: Boolean = false,
  val isGps: Boolean = false,
  val isPinned: Boolean = false,
  val isCritical: Boolean = false,
  /** Resolved drawable of the reminder's chosen icon, or null when none. */
  val iconRes: Int? = null,
  /** ARGB of the reminder's chosen tag color, or null when none. */
  val tagColor: Int? = null,
) {
  val hasTag: Boolean get() = iconRes != null || tagColor != null
}

data class UiReminderListActions(
  val canToggle: Boolean = false,
  val canOpen: Boolean = false,
  val canEdit: Boolean = false,
  val canDelete: Boolean = false,
  val canSkip: Boolean = false,
)
