package com.elementary.tasks.googletasks.task

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

data class EditGoogleTaskState(
  val title: String = "",
  val titleError: Boolean = false,
  val notes: String = "",
  val dateText: String? = null,
  val isDateSelected: Boolean = false,
  val timeText: String? = null,
  val isTimeSelected: Boolean = false,
  val listName: String = "",
  val isLoading: Boolean = false,
  val hasId: Boolean = false,
  val dialog: EditGoogleTaskDialog? = null,
)

sealed interface EditGoogleTaskDialog {
  data object DateTypeChooser : EditGoogleTaskDialog

  data object TimeTypeChooser : EditGoogleTaskDialog

  data class ListPicker(
    val options: List<GoogleTaskListOption>,
    val selectedId: String,
    val forMove: Boolean,
  ) : EditGoogleTaskDialog

  data object DeleteConfirm : EditGoogleTaskDialog
}

data class GoogleTaskListOption(
  val id: String,
  val title: String,
)

sealed interface EditGoogleTaskEvent {
  data class ShowDatePicker(
    val date: LocalDate,
  ) : EditGoogleTaskEvent

  data class ShowTimePicker(
    val time: LocalTime,
  ) : EditGoogleTaskEvent

  data object Saved : EditGoogleTaskEvent

  data object Deleted : EditGoogleTaskEvent
}
