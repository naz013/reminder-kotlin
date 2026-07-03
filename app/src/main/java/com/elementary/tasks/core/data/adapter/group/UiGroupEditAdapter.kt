package com.elementary.tasks.core.data.adapter.group

import com.elementary.tasks.core.data.ui.group.UiGroupEdit
import com.github.naz013.domain.ReminderGroup

class UiGroupEditAdapter {
  fun convert(reminderGroup: ReminderGroup): UiGroupEdit =
    UiGroupEdit(
      id = reminderGroup.groupUuId,
      colorPosition = reminderGroup.groupColor,
      title = reminderGroup.groupTitle,
      isDefault = reminderGroup.isDefaultGroup,
    )
}
