package com.elementary.tasks.core.fragments

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.arch.BindingFragment
import com.github.naz013.ui.common.theme.ThemeProvider
import com.elementary.tasks.core.utils.params.Prefs
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import org.koin.android.ext.android.inject

abstract class BaseMapFragment<B : ViewBinding> : BindingFragment<B>() {

  protected val themeUtil by inject<ThemeProvider>()
  protected val prefs by inject<Prefs>()

  private var internalMapType = GoogleMap.MAP_TYPE_TERRAIN
  private var internalMapStyle = 0

  protected fun setStyle(
    map: GoogleMap,
    mapType: Int = internalMapType,
    mapStyle: Int = prefs.mapStyle
  ) {
    internalMapType = mapType
    if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
      if (map.mapType == GoogleMap.MAP_TYPE_SATELLITE || map.mapType == GoogleMap.MAP_TYPE_HYBRID) {
        map.mapType = GoogleMap.MAP_TYPE_NONE
      }
      val ctx = context ?: return
      map.setMapStyle(
        MapStyleOptions.loadRawResourceStyle(ctx, themeUtil.getMapStyleJson(mapStyle))
      )
      map.mapType = mapType
    } else {
      map.mapType = mapType
    }
  }

  protected fun setMapType(map: GoogleMap, type: Int, function: (() -> Unit)?) {
    setStyle(map, type)
    prefs.mapType = type
    function?.invoke()
  }

  protected fun refreshStyles(map: GoogleMap) {
    setStyle(map, prefs.mapType)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    internalMapType = prefs.mapType
  }
}
