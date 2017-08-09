package com.elementary.tasks.places;

import com.elementary.tasks.core.network.RetrofitBuilder;
import com.elementary.tasks.core.network.places.PlacesResponse;
import com.elementary.tasks.core.utils.Module;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;

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

public class RequestBuilder {

    private RequestBuilder() {
    }

    public static Call<PlacesResponse> getNearby(double lat, double lng, String name) {
        String req = name.replaceAll("\\s+", "+");
        Map<String, String> params = new LinkedHashMap<>();
        params.put("location", lat + "," + lng);
        params.put("radius", "50000");
        params.put("name", req);
        params.put("language", getLanguage());
        params.put("key", getKey());
        return RetrofitBuilder.getPlacesApi().getNearbyPlaces(params);
    }

    public static Call<PlacesResponse> getSearch(String name) {
        String req = name.replaceAll("\\s+", "+");
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", req);
        params.put("language", getLanguage());
        params.put("key", getKey());
        return RetrofitBuilder.getPlacesApi().getPlaces(params);
    }

    private static String getLanguage() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage();
    }

    public static String getKey() {
        String key = "AIzaSyCMrJF6bn1Mt6n2uyLLLN85h-PGAtotT3Q";
        if (Module.isPro()) {
            key = "AIzaSyD80IRgaabOQoZ_mRP_RL36CJKeDO96yKw";
        }
        return key;
    }
}
