package com.elementary.tasks.core.services.usecase

import android.location.Location
import com.github.naz013.domain.Place
import kotlin.math.roundToInt

class PlaceDistanceCalculator {
  fun metersTo(
    location: Location,
    place: Place,
  ): Int {
    val destination =
      Location("destination").apply {
        latitude = place.latitude
        longitude = place.longitude
      }
    return location.distanceTo(destination).roundToInt()
  }
}
