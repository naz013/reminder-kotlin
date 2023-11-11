package com.elementary.tasks.core.data.adapter.group

import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.isColorDark

class UiGroupListAdapter(
  private val contextProvider: ContextProvider
) {

  fun convert(reminderGroup: ReminderGroup): UiGroupList {
    val groupColor = ThemeProvider.themedColor(contextProvider.context, reminderGroup.groupColor)
    return UiGroupList(
      id = reminderGroup.groupUuId,
      color = groupColor,
      title = reminderGroup.groupTitle,
      colorPosition = reminderGroup.groupColor,
      contrastColor = getContrastColor(groupColor)
    )
  }

  fun convert(id: String, colorPosition: Int, title: String?): UiGroupList {
    val groupColor = ThemeProvider.themedColor(contextProvider.context, colorPosition)
    return UiGroupList(
      id = id,
      color = groupColor,
      title = title ?: "",
      colorPosition = colorPosition,
      contrastColor = getContrastColor(groupColor)
    )
  }

  @ColorInt
  private fun getContrastColor(@ColorInt color: Int): Int {
    return if (color.isColorDark()) {
      ContextCompat.getColor(contextProvider.context, R.color.whitePrimary)
    } else {
      ContextCompat.getColor(contextProvider.context, R.color.pureBlack)
    }
  }
}
