package com.github.naz013.ui.reminder

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderV2

interface UiReminderListAdapter {
  fun createV2(
    data: ReminderV2,
    group: GroupV2?,
  ): UiReminderList
}
