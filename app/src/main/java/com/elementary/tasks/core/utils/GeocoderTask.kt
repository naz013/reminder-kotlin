package com.elementary.tasks.core.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.io.IOException

class GeocoderTask(
  private val context: Context,
  private val featureManager: FeatureManager
) {

  private var mJob: Job? = null

  fun findAddresses(address: String, listener: ((List<Address>) -> Unit)?) {
    if (!featureManager.isFeatureEnabled(FeatureManager.Feature.GEOCODING)) {
      listener?.invoke(emptyList())
      return
    }
    cancelJob()
    val geocoder = Geocoder(context)
    mJob = launchDefault {
      val addresses: MutableList<Address> = mutableListOf()
      try {
        addresses.addAll(geocoder.getFromLocationName(address, 5) ?: emptyList())
      } catch (_: IOException) {
      }
      withUIContext {
        listener?.invoke(addresses)
      }
      mJob = null
    }
  }

  private fun cancelJob() {
    mJob?.cancel()
  }

  fun getAddressForLocation(latLng: LatLng): String? {
    if (!featureManager.isFeatureEnabled(FeatureManager.Feature.GEOCODING)) {
      return null
    }
    val geocoder = Geocoder(context)
    return runBlocking(Dispatchers.IO) {
      val addresses = runCatching {
        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
      }.getOrNull()
      if (!addresses.isNullOrEmpty()) {
        addresses[0].toShortAddress()
      } else {
        null
      }
    }
  }

  private fun Address.toShortAddress(): String {
    val sb = StringBuilder()
    sb.append(featureName)
    if (adminArea != null) {
      sb.append(", ").append(adminArea)
    }
    if (countryName != null) {
      sb.append(", ").append(countryName)
    }
    return sb.toString()
  }
}
