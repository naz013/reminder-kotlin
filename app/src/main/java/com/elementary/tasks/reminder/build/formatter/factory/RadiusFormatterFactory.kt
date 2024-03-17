package com.elementary.tasks.reminder.build.formatter.factory

import android.content.Context
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.radius.DefaultRadiusFormatter

class RadiusFormatterFactory(
  private val context: Context,
  private val prefs: Prefs
) {

  fun create(): DefaultRadiusFormatter {
    return DefaultRadiusFormatter(
      context = context,
      useMetric = prefs.useMetric
    )
  }
}
