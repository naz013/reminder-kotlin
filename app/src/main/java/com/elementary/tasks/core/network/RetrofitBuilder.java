package com.elementary.tasks.core.network;

import com.elementary.tasks.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public final class RetrofitBuilder {

    private RetrofitBuilder (){}

    private static Retrofit retrofit;
    private static Retrofit retrofitPlaces;

    private static Retrofit getRetrofitBuilder(){
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(UnsplashApi.BASE_URL)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static Retrofit getPlacesBuilder(){
        if (retrofitPlaces == null) {
            retrofitPlaces = new Retrofit.Builder()
                    .baseUrl(PlacesApi.BASE_URL)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitPlaces;
    }

    private static UnsplashApi api = getRetrofitBuilder().create(UnsplashApi.class);

    private static PlacesApi placesApi = getPlacesBuilder().create(PlacesApi.class);

    public static PlacesApi getPlacesApi() {
        return placesApi;
    }

    public static UnsplashApi getUnsplashApi(){
        return api;
    }

    public static String getImageLink(long id){
        return UnsplashApi.BASE_URL + "/1280/768?image=" + id;
    }

    public static String getImageLink(long id, int width, int height){
        return UnsplashApi.BASE_URL + "/" + width + "/" + height + "?image=" + id;
    }

    private static OkHttpClient getClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        else interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        return new OkHttpClient.Builder()
                .addInterceptor(interceptor).build();
    }
}
