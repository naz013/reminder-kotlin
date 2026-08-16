package com.github.naz013.feature.reminder.build.formatter.factory

import android.content.Context
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.feature.reminder.util.DefaultRadiusFormatter

class RadiusFormatterFactory(
  private val context: Context,
  private val reminderPreferences: ReminderPreferences,
) {
  fun create(): DefaultRadiusFormatter =
    DefaultRadiusFormatter(
      context = context,
      useMetric = reminderPreferences.useMetric,
    )
}
