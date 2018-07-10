package com.elementary.tasks.places.google

import com.elementary.tasks.core.network.RetrofitBuilder
import com.elementary.tasks.core.network.places.PlacesResponse
import com.elementary.tasks.core.utils.Module

import java.util.LinkedHashMap
import java.util.Locale

import retrofit2.Call

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

object RequestBuilder {

    private val language: String
        get() {
            val locale = Locale.getDefault()
            return locale.language
        }

    val key: String
        get() {
            var key = "AIzaSyCMrJF6bn1Mt6n2uyLLLN85h-PGAtotT3Q"
            if (Module.isPro) {
                key = "AIzaSyD80IRgaabOQoZ_mRP_RL36CJKeDO96yKw"
            }
            return key
        }

    fun getNearby(lat: Double, lng: Double, name: String): Call<PlacesResponse> {
        val req = name.replace("\\s+".toRegex(), "+")
        val params = LinkedHashMap<String, String>()
        params["location"] = lat.toString() + "," + lng
        params["radius"] = "50000"
        params["name"] = req
        params["language"] = language
        params["key"] = key
        return RetrofitBuilder.placesApi.getNearbyPlaces(params)
    }

    fun getSearch(name: String): Call<PlacesResponse> {
        val req = name.replace("\\s+".toRegex(), "+")
        val params = LinkedHashMap<String, String>()
        params["query"] = req
        params["language"] = language
        params["key"] = key
        return RetrofitBuilder.placesApi.getPlaces(params)
    }
}
