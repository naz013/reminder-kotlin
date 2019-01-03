package com.elementary.tasks.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.elementary.tasks.ReminderApp

import com.elementary.tasks.core.utils.Prefs
import com.google.android.gms.location.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class LocationTracker(private val mContext: Context?, private val mCallback: ((lat: Double, lng: Double) -> Unit)?) {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            Timber.d("onLocationResult: $locationResult")
            for (location in locationResult!!.locations) {
                val latitude = location.latitude
                val longitude = location.longitude
                mCallback?.invoke(latitude, longitude)
                break
            }
        }
    }

    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
        updateListener()
    }

    fun removeUpdates() {
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun updateListener() {
        if (mContext == null) {
            return
        }
        val time = (prefs.trackTime * 1000 * 2).toLong()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        val locationRequest = LocationRequest()
        locationRequest.interval = time
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(mContext)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { mFusedLocationClient?.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper()) }
    }
}
