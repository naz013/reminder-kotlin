package com.elementary.tasks.places.google

import com.elementary.tasks.BuildConfig
import com.elementary.tasks.core.network.PlacesApi
import com.elementary.tasks.core.network.RetrofitBuilder
import com.elementary.tasks.core.network.places.PlacesResponse
import retrofit2.Call
import java.util.Locale

object RequestBuilder {

  private val language: String
    get() {
      val locale = Locale.getDefault()
      return locale.language
    }

  fun getNearby(lat: Double, lng: Double, name: String): Call<PlacesResponse> {
    val req = name.replace("\\s+".toRegex(), "+")
    val params = LinkedHashMap<String, String>()
    params["location"] = "$lat,$lng"
    params["radius"] = "50000"
    params["name"] = req
    params["language"] = language
    params["key"] = BuildConfig.PLACES_API_KEY
    return RetrofitBuilder.placesApi.getNearbyPlaces(params)
  }

  fun getSearch(name: String): Call<PlacesResponse> {
    val req = name.replace("\\s+".toRegex(), "+")
    var url = PlacesApi.BASE_URL + "textsearch/json?"
    url += "query=$req"
    url += "&inputtype=textquery"
    url += "&language=$language"
    url += "&key=${BuildConfig.PLACES_API_KEY}"
    return RetrofitBuilder.placesApi.getPlaces(url)
  }
}
