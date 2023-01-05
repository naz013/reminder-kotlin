package com.elementary.tasks.core.data.adapter.place

import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.ui.place.UiPlaceEdit

class UiPlaceEditAdapter {

  fun convert(data: Place): UiPlaceEdit {
    return UiPlaceEdit(
      marker = data.marker,
      id = data.id,
      name = data.name,
      lat = data.latitude,
      lng = data.longitude,
      radius = data.radius
    )
  }
}
