package com.elementary.tasks.reminder.build.formatter

import android.content.Context
import com.elementary.tasks.core.utils.VibrationPresets

class VibrationPatternFormatter(
  private val context: Context,
) : Formatter<List<Long>>() {
  override fun format(vibrationPattern: List<Long>): String =
    VibrationPresets.ALL.firstOrNull { it.pattern == vibrationPattern }
      ?.let { context.getString(it.nameRes) }
      ?: "NA"
}
