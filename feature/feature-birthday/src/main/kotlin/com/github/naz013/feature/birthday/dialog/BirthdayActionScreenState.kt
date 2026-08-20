package com.github.naz013.feature.birthday.dialog

import android.graphics.Bitmap
import com.github.naz013.feature.birthday.actions.BirthdayAction

internal data class BirthdayActionScreenState(
  val id: String,
  val header: BirthdayActionScreenHeader,
  val mainAction: BirthdayActionScreenActionItem,
  val secondaryActions: List<BirthdayActionScreenActionItem>,
)

internal data class BirthdayActionScreenActionItem(
  val action: BirthdayAction,
  val text: String,
  val iconRes: Int,
)

internal data class BirthdayActionScreenHeader(
  val text: String,
  val phoneNumber: String,
  val contactName: String?,
  val contactPhoto: Bitmap?,
  val birthdayDate: String,
  val age: String?,
)
