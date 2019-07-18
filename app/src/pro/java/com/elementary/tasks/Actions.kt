package com.elementary.tasks

object Actions {

    const val ACTION_PLAY = "com.elementary.tasks.pro.ACTION_PLAY"
    const val ACTION_STOP = "com.elementary.tasks.pro.ACTION_STOP"

    object Reminder {
        const val ACTION_SB_HIDE = "com.elementary.tasks.pro.HIDE"
        const val ACTION_SB_SHOW = "com.elementary.tasks.pro.SHOW"
        const val ACTION_RUN = "com.elementary.tasks.pro.reminder.RUN"
        const val ACTION_SNOOZE = "com.elementary.tasks.pro.reminder.SNOOZE"
        const val ACTION_SHOW_FULL = "com.elementary.tasks.pro.reminder.SHOW_SCREEN"
        const val ACTION_HIDE_SIMPLE = "com.elementary.tasks.pro.reminder.SIMPLE_HIDE"
        const val ACTION_EDIT_EVENT = "com.elementary.tasks.pro.reminder.EVENT_EDIT"
    }

    object Birthday {
        const val ACTION_SB_HIDE = "com.elementary.tasks.pro.birthday.HIDE"
        const val ACTION_SB_SHOW = "com.elementary.tasks.pro.birthday.SHOW"
        const val ACTION_CALL = "com.elementary.tasks.pro.birthday.CALL"
        const val ACTION_SMS = "com.elementary.tasks.pro.birthday.SMS"
        const val ACTION_SHOW_FULL = "com.elementary.tasks.pro.birthday.SHOW_SCREEN"
    }
}
