package com.github.naz013.ui.notification.settings

import android.content.Context

class VibrationPatternFormatter(
  private val context: Context,
) : Formatter<List<Long>>() {
  override fun format(vibrationPattern: List<Long>): String =
    VibrationPresets.ALL.firstOrNull { it.pattern == vibrationPattern }
      ?.let { context.getString(it.nameRes) }
      ?: "NA"
}
