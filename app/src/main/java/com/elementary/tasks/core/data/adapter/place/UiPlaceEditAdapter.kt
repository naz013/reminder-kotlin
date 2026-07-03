package com.elementary.tasks.core.data.adapter.place

import com.elementary.tasks.core.data.ui.place.UiPlaceEdit
import com.github.naz013.domain.Place

class UiPlaceEditAdapter {
  fun convert(data: Place): UiPlaceEdit =
    UiPlaceEdit(
      marker = data.marker,
      id = data.id,
      name = data.name,
      lat = data.latitude,
      lng = data.longitude,
      radius = data.radius,
    )
}
