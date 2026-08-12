package com.github.naz013.ui.birthday

import android.graphics.Bitmap

data class UiBirthdayShow(
  val uuId: String,
  val name: String,
  val ageFormatted: String?,
  val number: String,
  val photo: Bitmap?,
  val uniqueId: Int,
)
