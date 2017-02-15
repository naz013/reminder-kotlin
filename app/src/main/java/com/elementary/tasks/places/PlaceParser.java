package com.elementary.tasks.places;

import com.elementary.tasks.core.utils.LogUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

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
public class PlaceParser {

    private final static String TAG = "PlaceParser";
    private final static String ADDRESS = "formatted_address";
    private final static String VICINITY = "vicinity";
    private final static String NAME = "name";
    private final static String GEOMETRY = "geometry";
    private final static String LOCATION = "location";
    private final static String LAT = "lat";
    private final static String LNG = "lng";
    private final static String ICON = "icon";
    private final static String ID = "id";
    private final static String TYPES = "types";

    public GooglePlaceItem getDetails(JSONObject jsonObject) {
        GooglePlaceItem model = new GooglePlaceItem();
        try {
            LogUtil.d(TAG, "Details " + jsonObject.toString());
            if (jsonObject.has(NAME)) {
                model.setName(jsonObject.getString(NAME));
            }
            if (jsonObject.has(ID)) {
                model.setId(jsonObject.getString(ID));
            }
            if (jsonObject.has(ICON)) {
                model.setIcon(jsonObject.getString(ICON));
            }
            if (jsonObject.has(ADDRESS)) {
                model.setAddress(jsonObject.getString(ADDRESS));
            }
            if (jsonObject.has(VICINITY)) {
                model.setAddress(jsonObject.getString(VICINITY));
            }
            if (jsonObject.has(GEOMETRY)) {
                model.setPosition(getCoordinates(jsonObject.getJSONObject(GEOMETRY)));
            }
            if (jsonObject.has(TYPES)){
                Type collectionType = new TypeToken<ArrayList<String>>() {}.getType();
                try {
                    ArrayList<String> types =
                            new Gson().fromJson(jsonObject.get(TYPES).toString(), collectionType);
                    model.setTypes(types);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return model;
    }

    private LatLng getCoordinates(JSONObject jsonObject) {
        if (jsonObject.has(LOCATION)) {
            try {
                JSONObject object = jsonObject.getJSONObject(LOCATION);
                double lat = 0.0;
                double lng = 0.0;
                if (object.has(LAT)) {
                    lat = object.getDouble(LAT);
                }
                if (object.has(LNG)) {
                    lng = object.getDouble(LNG);
                }
                return new LatLng(lat, lng);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
