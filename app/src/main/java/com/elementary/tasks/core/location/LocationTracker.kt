package com.elementary.tasks.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.github.naz013.feature.common.android.SystemServiceProvider
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
  private val systemServiceProvider: SystemServiceProvider
) : LocationListener {

  private var mLocationManager: LocationManager? = null
  private var mFusedLocationClient: FusedLocationProviderClient? = null
  private val mLocationCallback = object : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
      Logger.d("onLocationResult: $locationResult")
      for (location in locationResult.locations) {
        val latitude = location.latitude
        val longitude = location.longitude
        listener.onUpdate(latitude, longitude)
        break
      }
    }
  }

  fun startUpdates() {
    updateListener()
  }

  fun removeUpdates() {
    mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    mLocationManager?.removeUpdates(this)
  }

  @SuppressLint("MissingPermission")
  private fun updateListener() {
    val time = (prefs.trackTime * 1000 * 2).toLong()
    val locationManager = systemServiceProvider.provideLocationManager()
    if (locationManager != null) {
      val criteria = Criteria()
      val bestProvider = locationManager.getBestProvider(criteria, false)
      if (bestProvider != null) {
        locationManager.requestLocationUpdates(
          bestProvider,
          time,
          3.0f,
          this,
          Looper.getMainLooper()
        )
      }
    }
    this.mLocationManager = locationManager

    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest()
    locationRequest.interval = time
    locationRequest.fastestInterval = 5000
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    val builder = LocationSettingsRequest.Builder()
      .addLocationRequest(locationRequest)
    val client = LocationServices.getSettingsClient(context)
    val task = client.checkLocationSettings(builder.build())
    task.addOnSuccessListener {
      mFusedLocationClient?.requestLocationUpdates(
        locationRequest,
        mLocationCallback,
        Looper.myLooper()
      )
    }
  }

  override fun onLocationChanged(location: Location) {
    Logger.d("onLocationResult: $location")
    val latitude = location.latitude
    val longitude = location.longitude
    listener.onUpdate(latitude, longitude)
  }

  @Deprecated("Deprecated in Java")
  override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
    Logger.d("onStatusChanged: $provider")
    updateListener()
  }

  override fun onProviderEnabled(provider: String) {
    Logger.d("onProviderEnabled: $provider")
    updateListener()
  }

  override fun onProviderDisabled(provider: String) {
    Logger.d("onProviderDisabled: $provider")
    updateListener()
  }

  interface Listener {
    fun onUpdate(lat: Double, lng: Double)
  }
}
