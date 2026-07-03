package com.elementary.tasks.birthdays.dialog

import android.graphics.Bitmap
import com.elementary.tasks.birthdays.actions.BirthdayAction

data class BirthdayActionScreenState(
  val id: String,
  val header: BirthdayActionScreenHeader,
  val mainAction: BirthdayActionScreenActionItem,
  val secondaryActions: List<BirthdayActionScreenActionItem>,
)

data class BirthdayActionScreenActionItem(
  val action: BirthdayAction,
  val text: String,
  val iconRes: Int,
)

data class BirthdayActionScreenHeader(
  val text: String,
  val phoneNumber: String,
  val contactName: String?,
  val contactPhoto: Bitmap?,
  val birthdayDate: String,
  val age: String?,
)
