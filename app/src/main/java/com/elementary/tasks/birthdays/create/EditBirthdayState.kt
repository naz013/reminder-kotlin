package com.elementary.tasks.birthdays.create

import android.graphics.Bitmap
import com.elementary.tasks.R
import com.github.naz013.ui.tag.TagChipState
import org.threeten.bp.LocalDate

data class EditBirthdayState(
  val id: String = "",
  val titleRes: Int = R.string.add_birthday,
  val name: String = "",
  val nameError: Boolean = false,
  val dateText: String = "",
  val ignoreYear: Boolean = false,
  val number: String = "",
  val contactName: String? = null,
  val contactPhoto: Bitmap? = null,
  val hasId: Boolean = false,
  val canDelete: Boolean = false,
  val isLoading: Boolean = false,
  val dialog: EditBirthdayDialog? = null,
  val selectedDate: LocalDate = LocalDate.now(),
  val hasSameInDb: Boolean = false,
  val isFromFile: Boolean = false,
  val allTags: List<TagChipState> = emptyList(),
  val selectedTagIds: Set<String> = emptySet(),
)

sealed interface EditBirthdayDialog {
  data object CopyConflict : EditBirthdayDialog

  data object DeleteConfirm : EditBirthdayDialog
}
