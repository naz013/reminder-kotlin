package com.github.naz013.feature.reminder.build.formatter.`object`

import com.github.naz013.ui.map.DefaultRadiusFormatter
import com.github.naz013.ui.notification.settings.Formatter
import com.github.naz013.domain.Place

internal class PlaceFormatter(
  private val radiusFormatter: DefaultRadiusFormatter,
) : Formatter<Place>() {
  override fun format(place: Place): String {
    val firstLine =
      place.address.takeIf { it.isNotEmpty() }
        ?: place.name.takeIf { it.isNotEmpty() }
        ?: "${place.latitude}, ${place.longitude}"
    val secondLine = radiusFormatter.format(place.radius)
    return "$firstLine\n$secondLine"
  }
}
