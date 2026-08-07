package com.elementary.tasks.reminder.preview

import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace
import com.elementary.tasks.core.data.ui.reminder.UiReminderStatus
import com.elementary.tasks.core.data.ui.reminder.UiReminderTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.AttachmentFile
import com.elementary.tasks.reminder.preview.data.UiCalendarEventList

data class PreviewReminderState(
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
  val showDeleteConfirm: Boolean = false,
)

data class UiPreviewSubTask(
  val id: String,
  val text: String,
  val isChecked: Boolean,
)
