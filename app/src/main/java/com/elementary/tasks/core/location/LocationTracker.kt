package com.elementary.tasks.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logging.Logger
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

class LocationTracker(
  private val listener: Listener,
  private val prefs: Prefs,
  private val context: Context,
) {
  private var mFusedLocationClient: FusedLocationProviderClient? = null
  private var isTracking = false
  private val mLocationCallback =
    object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult) {
        Logger.d(TAG, "onLocationResult: $locationResult")
        val location = locationResult.locations.firstOrNull() ?: return
        listener.onUpdate(location.latitude, location.longitude)
      }
    }

  fun startUpdates() {
    if (isTracking) {
      Logger.d(TAG, "startUpdates: already tracking, ignoring")
      return
    }
    isTracking = true
    updateListener()
  }

  fun removeUpdates() {
    isTracking = false
    mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
  }

  @SuppressLint("MissingPermission")
  private fun updateListener() {
    val time = (prefs.trackTime * 1000 * 2).toLong()
    val client = LocationServices.getFusedLocationProviderClient(context)
    mFusedLocationClient = client

    val locationRequest = LocationRequest()
    locationRequest.interval = time
    locationRequest.fastestInterval = 5000
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    val builder =
      LocationSettingsRequest
        .Builder()
        .addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(context)
    settingsClient.checkLocationSettings(builder.build()).addOnSuccessListener {
      client.requestLocationUpdates(
        locationRequest,
        mLocationCallback,
        Looper.getMainLooper(),
      )
    }
  }

  interface Listener {
    fun onUpdate(
      lat: Double,
      lng: Double,
    )
  }

  companion object {
    private const val TAG = "LocationTracker"
  }
}
