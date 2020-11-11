package com.elementary.tasks.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.elementary.tasks.core.utils.Prefs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

@KoinApiExtension
class LocationTracker(
  private val context: Context?,
  private val callback: ((lat: Double, lng: Double) -> Unit)?
) : LocationListener, KoinComponent {

  private var mLocationManager: LocationManager? = null
  private var mFusedLocationClient: FusedLocationProviderClient? = null
  private val mLocationCallback = object : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult?) {
      Timber.d("onLocationResult: $locationResult")
      for (location in locationResult!!.locations) {
        val latitude = location.latitude
        val longitude = location.longitude
        callback?.invoke(latitude, longitude)
        break
      }
    }
  }

  private val prefs: Prefs by inject()

  init {
    updateListener()
  }

  fun removeUpdates() {
    mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    mLocationManager?.removeUpdates(this)
  }

  @SuppressLint("MissingPermission")
  private fun updateListener() {
    if (context == null) {
      return
    }
    val time = (prefs.trackTime * 1000 * 2).toLong()
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
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
    task.addOnSuccessListener { mFusedLocationClient?.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper()) }
  }

  override fun onLocationChanged(location: Location) {
    Timber.d("onLocationResult: $location")
    val latitude = location.latitude
    val longitude = location.longitude
    callback?.invoke(latitude, longitude)
  }

  override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
    Timber.d("onStatusChanged: $provider")
    updateListener()
  }

  override fun onProviderEnabled(provider: String) {
    Timber.d("onProviderEnabled: $provider")
    updateListener()
  }

  override fun onProviderDisabled(provider: String) {
    Timber.d("onProviderDisabled: $provider")
    updateListener()
  }
}
