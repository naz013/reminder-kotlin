package com.elementary.tasks.core.network

import com.elementary.tasks.core.network.places.PlacesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface PlacesApi {

    @GET("nearbysearch/json?")
    fun getNearbyPlaces(@QueryMap params: Map<String, String>): Call<PlacesResponse>

    @GET
    fun getPlaces(@Url url: String): Call<PlacesResponse>

    companion object {
        const val OK = 200

        const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"
    }
}
