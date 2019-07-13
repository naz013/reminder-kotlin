package com.elementary.tasks

class Actions {

    object Reminder {
        const val ACTION_SB_HIDE = "com.elementary.tasks.HIDE"
        const val ACTION_SB_SHOW = "com.elementary.tasks.SHOW"
        const val ACTION_RUN = "com.elementary.tasks.reminder.RUN"
        const val ACTION_SHOW_FULL = "com.elementary.tasks.reminder.SHOW_SCREEN"
        const val ACTION_HIDE_SIMPLE = "com.elementary.tasks.reminder.SIMPLE_HIDE"
        const val ACTION_EDIT_EVENT = "com.elementary.tasks.reminder.EVENT_EDIT"
    }

    object Birthday {
        const val ACTION_SB_HIDE = "com.elementary.tasks.birthday.HIDE"
        const val ACTION_SB_SHOW = "com.elementary.tasks.birthday.SHOW"
        const val ACTION_CALL = "com.elementary.tasks.birthday.CALL"
        const val ACTION_SMS = "com.elementary.tasks.birthday.SMS"
        const val ACTION_SHOW_FULL = "com.elementary.tasks.birthday.SHOW_SCREEN"
    }
}
