package com.elementary.tasks.experimental.home

import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.view_models.BaseDbViewModel

class HomeViewModel : BaseDbViewModel() {

    val reminders = appDb.reminderDao().loadAllTypesInRange(limit = 3,
            fromTime = TimeUtil.getDayStart(), toTime = TimeUtil.getDayEnd())

    val birthdays = appDb.birthdaysDao().loadAll(TimeUtil.getBirthdayDayMonth())

}
