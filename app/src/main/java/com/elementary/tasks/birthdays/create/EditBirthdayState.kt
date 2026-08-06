package com.elementary.tasks.birthdays.create

import android.graphics.Bitmap
import com.elementary.tasks.R
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
)

sealed interface EditBirthdayDialog {
  data object CopyConflict : EditBirthdayDialog

  data object DeleteConfirm : EditBirthdayDialog
}
