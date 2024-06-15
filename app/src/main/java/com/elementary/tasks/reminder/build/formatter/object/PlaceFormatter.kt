package com.elementary.tasks.reminder.build.formatter.`object`

import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.ui.radius.DefaultRadiusFormatter
import com.elementary.tasks.reminder.build.formatter.Formatter

class PlaceFormatter(
  private val radiusFormatter: DefaultRadiusFormatter
) : Formatter<Place>() {

  override fun format(place: Place): String {
    val firstLine = place.address.takeIf { it.isNotEmpty() }
      ?: place.name.takeIf { it.isNotEmpty() }
      ?: "${place.latitude}, ${place.longitude}"
    val secondLine = radiusFormatter.format(place.radius)
    return "$firstLine\n$secondLine"
  }
}
