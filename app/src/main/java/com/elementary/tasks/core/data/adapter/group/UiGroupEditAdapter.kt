package com.elementary.tasks.core.data.adapter.group

import com.github.naz013.domain.ReminderGroup
import com.elementary.tasks.core.data.ui.group.UiGroupEdit

class UiGroupEditAdapter {

  fun convert(reminderGroup: ReminderGroup): UiGroupEdit {
    return UiGroupEdit(
      id = reminderGroup.groupUuId,
      colorPosition = reminderGroup.groupColor,
      title = reminderGroup.groupTitle,
      isDefault = reminderGroup.isDefaultGroup
    )
  }
}
