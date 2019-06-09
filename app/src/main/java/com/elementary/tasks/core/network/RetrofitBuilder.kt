package com.elementary.tasks.core.network

import com.elementary.tasks.BuildConfig

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {

    private var retrofitPlaces: Retrofit? = null

    private val placesBuilder: Retrofit
        get() {
            if (retrofitPlaces == null) {
                retrofitPlaces = Retrofit.Builder()
                        .baseUrl(PlacesApi.BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
            }
            return retrofitPlaces!!
        }

    val placesApi: PlacesApi = placesBuilder.create(PlacesApi::class.java)

    private val client: OkHttpClient
        get() {
            val interceptor = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                interceptor.level = HttpLoggingInterceptor.Level.BODY
            } else {
                interceptor.level = HttpLoggingInterceptor.Level.NONE
            }
            return OkHttpClient.Builder()
                    .addInterceptor(interceptor).build()
        }
}
