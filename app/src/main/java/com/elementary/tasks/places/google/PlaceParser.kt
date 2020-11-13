package com.elementary.tasks.places.google

import com.elementary.tasks.core.network.places.Location
import com.elementary.tasks.core.network.places.Place
import com.google.android.gms.maps.model.LatLng

object PlaceParser {

  fun getDetails(place: Place): GooglePlaceItem {
    val model = GooglePlaceItem()
    model.name = place.name ?: ""
    model.id = place.id
    model.icon = place.icon ?: ""
    model.address = place.formattedAddress ?: ""
    model.position = getCoordinates(place.geometry?.location)
    model.types = place.types
    return model
  }

  private fun getCoordinates(location: Location?): LatLng? {
    return if (location != null) {
      LatLng(location.lat, location.lng)
    } else null
  }
}
