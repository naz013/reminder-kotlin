package com.elementary.tasks.places;

import com.elementary.tasks.core.utils.Module;

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

    public static String getNearby(double lat, double lng, String name) {
        String key = "AIzaSyCMrJF6bn1Mt6n2uyLLLN85h-PGAtotT3Q";
        if (Module.isPro()) key = "AIzaSyD80IRgaabOQoZ_mRP_RL36CJKeDO96yKw";
        key = "&key=" + key;
        String req = name.replaceAll("\\s+", "+");
        String params = "location=" + lat + "," + lng + req + "&radius=50000&name=" + name;
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + params + key;
    }

    public static String getSearch(String name) {
        String key = "AIzaSyCMrJF6bn1Mt6n2uyLLLN85h-PGAtotT3Q";
        if (Module.isPro()) key = "AIzaSyD80IRgaabOQoZ_mRP_RL36CJKeDO96yKw";
        key = "&key=" + key;
        String req = name.replaceAll("\\s+", "+");
        String params = "query=" + req;
        return "https://maps.googleapis.com/maps/api/place/textsearch/json?" + params + key;
    }
}
