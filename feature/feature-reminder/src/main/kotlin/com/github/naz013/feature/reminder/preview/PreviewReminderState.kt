package com.github.naz013.feature.reminder.preview

import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.github.naz013.feature.reminder.note.UiNoteList
import com.github.naz013.ui.reminder.UiReminderPlace
import com.github.naz013.ui.reminder.UiReminderStatus
import com.github.naz013.ui.reminder.UiReminderTarget
import com.github.naz013.ui.reminder.UiReminderType
import com.github.naz013.feature.reminder.build.valuedialog.controller.attachments.AttachmentFile
import com.github.naz013.feature.reminder.preview.data.UiCalendarEventList
import com.github.naz013.ui.tag.TagChipState

internal data class PreviewReminderState(
  val id: String = "",
  val isLoading: Boolean = true,
  val status: UiReminderStatus? = null,
  val summary: String = "",
  val description: String? = null,
  val dueDateTime: String? = null,
  val before: String? = null,
  val repeat: String = "",
  val remaining: String? = null,
  val groupTitle: String? = null,
  val priorityTitle: String = "",
  val target: UiReminderTarget? = null,
  val targetType: UiReminderType? = null,
  val rawTarget: String = "",
  val attachments: List<AttachmentFile> = emptyList(),
  val subTasks: List<UiPreviewSubTask> = emptyList(),
  val places: List<UiReminderPlace> = emptyList(),
  val placesHeader: String = "",
  val note: UiNoteList? = null,
  val googleTask: GoogleTaskItemState? = null,
  val calendarEvents: List<UiCalendarEventList> = emptyList(),
  val canCopy: Boolean = false,
  val canDelete: Boolean = false,
  val isPinned: Boolean = false,
  val showSyncToCloud: Boolean = false,
  val showDeleteConfirm: Boolean = false,
  val tags: List<TagChipState> = emptyList(),
)

internal data class UiPreviewSubTask(
  val id: String,
  val text: String,
  val isChecked: Boolean,
)
