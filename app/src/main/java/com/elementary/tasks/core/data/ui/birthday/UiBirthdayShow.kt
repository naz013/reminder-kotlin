package com.elementary.tasks.core.data.ui.birthday

import android.net.Uri

data class UiBirthdayShow(
  val uuId: String,
  val name: String,
  val ageFormatted: String,
  val number: String,
  val photo: Uri?,
  val uniqueId: Int
)
