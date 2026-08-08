package com.elementary.tasks.module.locationapi

import android.content.Context
import com.elementary.tasks.core.services.GeolocationService
import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.location.LocationTrackingApi
import com.github.naz013.logging.Logger

class LocationTrackingApiImpl(
  private val context: Context,
) : LocationTrackingApi {

  override fun startTracking() {
    SuperUtil.startGpsTracking(context)
    Logger.i(TAG, "Started the Location tracking")
  }

  override fun stopTracking() {
    SuperUtil.stopService(context, GeolocationService::class.java)
    Logger.i(TAG, "Stopped the Location tracking")
  }

  companion object {
    private const val TAG = "LocationTrackingApi"
  }
}
