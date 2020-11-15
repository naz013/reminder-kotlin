package com.elementary.tasks.reminder.create.fragments

import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.IntervalUtil
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.StringResPatterns

abstract class RepeatableTypeFragment<B : ViewDataBinding> : TypeFragment<B>() {

  override fun getSummary(): String {
    val reminder = iFace.reminderState.reminder
    var summary = ""
    val groupName = reminder.groupTitle
    if (groupName != "") {
      summary += "$groupName, "
    }
    summary += ReminderUtils.getPriorityTitle(requireContext(), reminder.priority) + ", "
    if (reminder.remindBefore > 0) {
      summary += IntervalUtil.getBeforeTime(reminder.remindBefore) {
        StringResPatterns.getBeforePattern(requireContext(), it)
      } + ", "
    }
    return summary
  }

  protected fun validBefore(millis: Long, reminder: Reminder): Boolean {
    if ((millis - reminder.remindBefore - 100) < System.currentTimeMillis()) {
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
