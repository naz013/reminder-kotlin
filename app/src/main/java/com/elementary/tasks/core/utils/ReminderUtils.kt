package com.elementary.tasks.core.utils

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs

object ReminderUtils {

  const val DAY_CHECKED = 1

  @Deprecated("Use DateTimeManager")
  fun getRepeatString(context: Context, prefs: Prefs, repCode: List<Int>): String {
    val sb = StringBuilder()
    val first = prefs.startDay
    if (first == 0 && repCode[0] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.sun))
    }
    if (repCode[1] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.mon))
    }
    if (repCode[2] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.tue))
    }
    if (repCode[3] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.wed))
    }
    if (repCode[4] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.thu))
    }
    if (repCode[5] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.fri))
    }
    if (repCode[6] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.sat))
    }
    if (first == 1 && repCode[0] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.sun))
    }
    return if (isAllChecked(repCode)) {
      context.getString(R.string.everyday)
    } else {
      sb.toString().trim()
    }
  }

  private fun isAllChecked(repCode: List<Int>): Boolean {
    return repCode.none { it == 0 }
  }
}
