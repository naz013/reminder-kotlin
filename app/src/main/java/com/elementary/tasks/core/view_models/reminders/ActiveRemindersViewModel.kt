package com.elementary.tasks.core.view_models.reminders

class ActiveRemindersViewModel : BaseRemindersViewModel() {
    val events = appDb.reminderDao().loadNotRemoved(false)
}
