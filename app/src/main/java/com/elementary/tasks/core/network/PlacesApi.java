package com.elementary.tasks.core.network;

import com.elementary.tasks.core.network.places.PlacesResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

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

public interface PlacesApi {
    int OK = 200;

    String BASE_URL = "https://maps.googleapis.com/maps/api/place/";

    @GET("nearbysearch/json?")
    Call<PlacesResponse> getNearbyPlaces(@QueryMap Map<String, String> params);

    @GET("textsearch/json?")
    Call<PlacesResponse> getPlaces(@QueryMap Map<String, String> params);
}
