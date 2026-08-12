package com.github.naz013.ui.notification.settings

import android.content.Context
import com.github.naz013.ui.common.R

class DelayMinutesFormatter(
  private val context: Context,
) : Formatter<Int>() {
  override fun format(delayMinutes: Int): String = context.getString(R.string.x_minutes, delayMinutes.toString())
}
