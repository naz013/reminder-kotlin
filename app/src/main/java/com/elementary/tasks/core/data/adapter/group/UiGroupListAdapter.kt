package com.elementary.tasks.core.data.adapter.group

import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider

class UiGroupListAdapter(
  private val contextProvider: ContextProvider
) {

  fun convert(reminderGroup: ReminderGroup): UiGroupList {
    return UiGroupList(
      id = reminderGroup.groupUuId,
      color = ThemeProvider.themedColor(contextProvider.context, reminderGroup.groupColor),
      title = reminderGroup.groupTitle,
      colorPosition = reminderGroup.groupColor
    )
  }

  fun convert(id: String, colorPosition: Int, title: String?): UiGroupList {
    return UiGroupList(
      id = id,
      color = ThemeProvider.themedColor(contextProvider.context, colorPosition),
      title = title ?: "",
      colorPosition = colorPosition
    )
  }
}
