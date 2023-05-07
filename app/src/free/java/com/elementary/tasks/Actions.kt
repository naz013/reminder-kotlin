package com.elementary.tasks

object Actions {

  const val ACTION_PLAY = "com.elementary.tasks.ACTION_PLAY"
  const val ACTION_STOP = "com.elementary.tasks.ACTION_STOP"
  const val ACTION_FORCE = "com.elementary.tasks.ACTION_FORCE"

  object Reminder {
    const val ACTION_SB_HIDE = "com.elementary.tasks.HIDE"
    const val ACTION_SB_SHOW = "com.elementary.tasks.SHOW"

    const val ACTION_SNOOZE = "com.elementary.tasks.reminder.SNOOZE"
    const val ACTION_HIDE_SIMPLE = "com.elementary.tasks.reminder.SIMPLE_HIDE"

    const val ACTION_EDIT_EVENT = "com.elementary.tasks.reminder.EVENT_EDIT"
  }

  object Birthday {
    const val ACTION_SB_HIDE = "com.elementary.tasks.birthday.HIDE"
    const val ACTION_SB_SHOW = "com.elementary.tasks.birthday.SHOW"

    const val ACTION_CALL = "com.elementary.tasks.birthday.CALL"
    const val ACTION_SMS = "com.elementary.tasks.birthday.SMS"
    const val ACTION_HIDE_SIMPLE = "com.elementary.tasks.birthday.SIMPLE_HIDE"
  }
}
