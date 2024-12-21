package com.github.naz013.domain.reminder

import com.google.gson.annotations.SerializedName

data class BuilderSchemeItem(
  @SerializedName("type")
  val type: BiType,
  @SerializedName("position")
  val position: Int
)
