package com.elementary.tasks.core.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Job
import java.io.IOException

object GeocoderTask {

    private var mJob: Job? = null

    fun findAddresses(context: Context, address: String, listener: ((List<Address>) -> Unit)?) {
        cancelJob()
        val geocoder = Geocoder(context)
        mJob = launchDefault {
            val addresses: MutableList<Address> = mutableListOf()
            try {
                addresses.addAll(geocoder.getFromLocationName(address, 5))
            } catch (e: IOException) {
            }
            withUIContext {
                listener?.invoke(addresses)
            }
            mJob = null
        }
    }

    fun cancelJob() {
        mJob?.cancel()
    }
}
