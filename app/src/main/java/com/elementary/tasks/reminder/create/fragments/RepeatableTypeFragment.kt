package com.elementary.tasks.reminder.create.fragments

import androidx.viewbinding.ViewBinding
import com.github.naz013.domain.Reminder
import org.threeten.bp.LocalDateTime

abstract class RepeatableTypeFragment<B : ViewBinding> : TypeFragment<B>() {

  protected fun validBefore(dateTime: LocalDateTime, reminder: Reminder): Boolean {
    val millis = dateTimeManager.toMillis(dateTime) - reminder.remindBefore - 100
    return millis >= System.currentTimeMillis()
  }

  protected fun getZeroedInt(v: Int): String {
    return if (v <= 9) {
      "0$v"
    } else {
      v.toString()
    }
  }
}
