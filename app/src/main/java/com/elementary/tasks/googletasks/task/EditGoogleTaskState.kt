package com.elementary.tasks.googletasks.task

import com.elementary.tasks.R
import com.github.naz013.domain.GoogleTaskList
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.util.UUID

data class EditGoogleTaskState(
  val taskId: String = UUID.randomUUID().toString(),
  val title: String = "",
  val titleError: Boolean = false,
  val notes: String = "",
  val dateText: String? = null,
  val isDateSelected: Boolean = false,
  val timeText: String? = null,
  val isTimeSelected: Boolean = false,
  val listName: String = "",
  val initialListId: String = "",
  val listId: String = "",
  val isLoading: Boolean = false,
  val dialog: EditGoogleTaskDialog? = null,
  val reminderId: String? = null,
  val date: LocalDate = LocalDate.now(),
  val time: LocalTime = LocalTime.now(),
  val googleTaskLists: List<GoogleTaskList> = emptyList(),
  val canMove: Boolean = false,
  val canDelete: Boolean = false,
  val screenTitleRes: Int = R.string.new_task,
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
