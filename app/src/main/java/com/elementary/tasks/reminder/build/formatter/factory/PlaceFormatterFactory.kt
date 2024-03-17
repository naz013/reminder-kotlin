package com.elementary.tasks.reminder.build.formatter.factory

import com.elementary.tasks.reminder.build.formatter.PlaceFormatter

class PlaceFormatterFactory(
  private val radiusFormatterFactory: RadiusFormatterFactory
) {

  fun create(): PlaceFormatter {
    return PlaceFormatter(radiusFormatterFactory.create())
  }
}
