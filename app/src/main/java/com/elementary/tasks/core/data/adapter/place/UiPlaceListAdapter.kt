package com.elementary.tasks.core.data.adapter.place

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.ui.DrawableHelper
import com.google.android.gms.maps.model.LatLng

class UiPlaceListAdapter(
  private val contextProvider: ContextProvider,
  private val themeProvider: ThemeProvider,
  private val dateTimeManager: DateTimeManager
) {

  fun convert(data: Place): UiPlaceList {
    val marker = DrawableHelper.withContext(contextProvider.context)
      .withDrawable(R.drawable.ic_fluent_place)
      .withColor(themeProvider.getMarkerLightColor(data.marker))
      .tint()
      .get()

    return UiPlaceList(
      marker = marker,
      id = data.id,
      name = data.name,
      latLng = LatLng(data.latitude, data.longitude),
      markerStyle = data.marker,
      formattedDate = dateTimeManager.getPlaceDateTimeFromGmt(data.dateTime)?.let {
        dateTimeManager.getDate(it)
      }
    )
  }
}
