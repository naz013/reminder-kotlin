package com.elementary.tasks.core.data.adapter.group

import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider

class UiGroupListAdapter(
  private val contextProvider: ContextProvider,
) {
  fun convert(reminderGroup: ReminderGroup): UiGroupList {
    val groupColor =
      ThemeProvider.themedColor(
        contextProvider.themedContext,
        reminderGroup.groupColor,
      )
    return UiGroupList(
      id = reminderGroup.groupUuId,
      color = groupColor,
      title = reminderGroup.groupTitle,
      colorPosition = reminderGroup.groupColor,
      contrastColor = getContrastColor(groupColor),
      isDefaultGroup = reminderGroup.isDefaultGroup,
      canDelete = !reminderGroup.isDefaultGroup,
      canSetAsDefault = !reminderGroup.isDefaultGroup,
    )
  }

  fun convert(group: GroupV2): UiGroupList {
    val groupColor = ThemeProvider.themedColor(contextProvider.themedContext, group.color)
    return UiGroupList(
      id = group.uuId,
      color = groupColor,
      title = group.title,
      colorPosition = group.color,
      contrastColor = getContrastColor(groupColor),
      isDefaultGroup = group.isDefault,
      canDelete = !group.isDefault,
      canSetAsDefault = !group.isDefault,
    )
  }

  fun convert(
    id: String,
    colorPosition: Int,
    title: String?,
  ): UiGroupList? {
    if (title.isNullOrBlank()) return null
    val groupColor = ThemeProvider.themedColor(contextProvider.themedContext, colorPosition)
    return UiGroupList(
      id = id,
      color = groupColor,
      title = title,
      colorPosition = colorPosition,
      contrastColor = getContrastColor(groupColor),
      isDefaultGroup = false,
      canDelete = false,
      canSetAsDefault = true,
    )
  }

  @ColorInt
  private fun getContrastColor(
    @ColorInt color: Int,
  ): Int =
    if (color.isColorDark()) {
      ContextCompat.getColor(contextProvider.themedContext, R.color.whitePrimary)
    } else {
      ContextCompat.getColor(contextProvider.themedContext, R.color.pureBlack)
    }
}
