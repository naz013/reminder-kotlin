package com.github.naz013.feature.reminder.build.formatter.factory

import com.github.naz013.feature.reminder.build.formatter.`object`.PlaceFormatter

internal class PlaceFormatterFactory(
  private val radiusFormatterFactory: RadiusFormatterFactory,
) {
  fun create(): PlaceFormatter = PlaceFormatter(radiusFormatterFactory.create())
}
