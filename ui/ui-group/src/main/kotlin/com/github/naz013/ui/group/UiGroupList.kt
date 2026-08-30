package com.github.naz013.ui.group

import androidx.annotation.ColorInt

data class UiGroupList(
  val id: String,
  val title: String,
  @param:ColorInt
  val color: Int,
  val colorPosition: Int,
  @Deprecated("Use compose color contrast color")
  val contrastColor: Int,
  val isDefaultGroup: Boolean,
  val canDelete: Boolean,
  val canSetAsDefault: Boolean,
  val reminderCountText: String = "",
  val isSelected: Boolean = false,
)
