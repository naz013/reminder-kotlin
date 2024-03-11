package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.R

class WindowTypeFormatter(
  private val context: Context
) : Formatter<Int>() {

  override fun format(windowType: Int): String {
    return if (windowType == FULL_SCREEN) {
      context.getString(R.string.full_screen)
    } else {
      context.getString(R.string.simple)
    }
  }

  companion object {
    const val FULL_SCREEN = 0
    const val NOTIFICATION = 1
  }
}
