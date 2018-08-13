package com.elementary.tasks.reminder

import com.elementary.tasks.core.data.models.*

class ReminderComposed {

    var reminder: Reminder? = null
    var reminderGroup: ReminderGroup? = null
    var googleTask: GoogleTask? = null
    var googleTaskList: GoogleTaskList? = null
    var note: Note? = null

    override fun toString(): String {
        return "ReminderComposed(reminder=$reminder, reminderReminderGroup=$reminderGroup, note=$note)"
    }
}