package com.elementary.tasks.reminder.create.fragments

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.data.models.Reminder
import org.threeten.bp.LocalDateTime

abstract class RepeatableTypeFragment<B : ViewBinding> : TypeFragment<B>() {

  protected fun validBefore(dateTime: LocalDateTime, reminder: Reminder): Boolean {
    if ((dateTimeManager.toMillis(dateTime) - reminder.remindBefore - 100) < System.currentTimeMillis()) {
      return false
    }
    return true
  }

  protected fun getZeroedInt(v: Int): String {
    return if (v <= 9) {
      "0$v"
    } else {
      v.toString()
    }
  }
}
