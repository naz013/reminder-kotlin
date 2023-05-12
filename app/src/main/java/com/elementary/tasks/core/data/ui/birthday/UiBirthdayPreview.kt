package com.elementary.tasks.core.data.ui.birthday

import android.graphics.Bitmap

data class UiBirthdayPreview(
  val uuId: String,
  val name: String,
  val number: String?,
  val photo: Bitmap?,
  val contactName: String?,
  val ageFormatted: String?,
  val dateOfBirth: String?,
  val nextBirthdayDate: String?,
  val hasBirthdayToday: Boolean
)
