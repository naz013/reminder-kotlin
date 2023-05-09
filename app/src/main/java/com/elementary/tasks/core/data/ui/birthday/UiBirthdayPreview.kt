package com.elementary.tasks.core.data.ui.birthday

import android.net.Uri

data class UiBirthdayPreview(
  val uuId: String,
  val name: String,
  val number: String?,
  val photo: Uri?,
  val contactName: String?,
  val ageFormatted: String?,
  val dateOfBirth: String?,
  val nextBirthdayDate: String?,
  val hasBirthdayToday: Boolean
)
