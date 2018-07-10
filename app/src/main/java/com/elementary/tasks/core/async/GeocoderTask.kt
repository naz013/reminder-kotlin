package com.elementary.tasks.core.async

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask

import com.elementary.tasks.core.utils.LogUtil

import java.io.IOException

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

class GeocoderTask(mContext: Context, private val mListener: GeocoderListener?) : AsyncTask<String, Void, List<Address>>() {
    private val geocoder: Geocoder

    init {
        geocoder = Geocoder(mContext)
    }

    override fun doInBackground(vararg locationName: String): List<Address>? {
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocationName(locationName[0], 5)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addresses
    }

    override fun onPostExecute(addresses: List<Address>?) {
        if (addresses == null || addresses.size == 0) {
            LogUtil.d(TAG, "No Location found")
        } else {
            mListener?.onAddressReceived(addresses)
        }
    }

    /**
     * Listener for found places list.
     */
    interface GeocoderListener {
        fun onAddressReceived(addresses: List<Address>)
    }

    companion object {

        private val TAG = "GeocoderTask"
    }
}
