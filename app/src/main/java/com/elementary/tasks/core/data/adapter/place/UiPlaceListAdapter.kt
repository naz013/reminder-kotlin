package com.elementary.tasks.core.data.adapter.place

import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.ui.DrawableHelper
import com.github.naz013.domain.Place
import com.github.naz013.common.ContextProvider
import com.google.android.gms.maps.model.LatLng

class UiPlaceListAdapter(
  private val contextProvider: ContextProvider,
  private val themeProvider: ThemeProvider,
  private val dateTimeManager: DateTimeManager
) {

  fun convert(data: Place): UiPlaceList {
    val marker = DrawableHelper.withContext(contextProvider.themedContext)
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
