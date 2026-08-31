package com.github.naz013.ui.group

import androidx.annotation.ColorInt
import com.github.naz013.ui.common.selection.Selectable

data class UiGroupList(
  override val id: String,
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
  /** Whether this group is currently open in the two-pane layout's detail pane. */
  val isHighlighted: Boolean = false,
  override val isSelected: Boolean = false,
) : Selectable<UiGroupList> {
  override fun withSelected(selected: Boolean) = copy(isSelected = selected)
}
