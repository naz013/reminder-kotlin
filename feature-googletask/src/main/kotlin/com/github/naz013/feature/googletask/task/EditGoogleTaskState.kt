package com.github.naz013.feature.googletask.task

import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.ui.common.R
import com.github.naz013.ui.tag.TagChipState
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.util.UUID

internal data class EditGoogleTaskState(
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
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagIds: Set<String> = emptySet(),
)

internal sealed interface EditGoogleTaskDialog {
  data object DateTypeChooser : EditGoogleTaskDialog

  data object TimeTypeChooser : EditGoogleTaskDialog

  data class ListPicker(
    val options: List<GoogleTaskListOption>,
    val selectedId: String,
    val forMove: Boolean,
  ) : EditGoogleTaskDialog

  data object DeleteConfirm : EditGoogleTaskDialog
}

internal data class GoogleTaskListOption(
  val id: String,
  val title: String,
)
