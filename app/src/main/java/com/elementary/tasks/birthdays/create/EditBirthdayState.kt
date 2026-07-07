package com.elementary.tasks.birthdays.create

import android.graphics.Bitmap

data class EditBirthdayState(
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
)

sealed interface EditBirthdayDialog {
  data object CopyConflict : EditBirthdayDialog

  data object DeleteConfirm : EditBirthdayDialog
}
