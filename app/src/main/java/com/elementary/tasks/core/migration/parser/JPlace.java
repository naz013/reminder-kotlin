package com.elementary.tasks.core.migration.parser;

import com.elementary.tasks.core.data.models.Place;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2017 Nazar Suhovich
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

class JPlace {

    /**
     * JSON keys.
     */
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String MARKER = "marker";
    private static final String RADIUS = "radius";
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String ADDRESS = "address";
    private static final String TYPES = "types";

    private int radius, marker;
    private double latitude, longitude;
    private String name, id, address;
    private List<String> types = new ArrayList<>();

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    public Place getPlace() {
        return new Place(radius, marker, latitude, longitude, name, address, types);
    }

    /**
     * Get current JSON object.
     *
     * @return JSON object string
     */
    @Override
    public String toString() {
        if (jsonObject != null) return jsonObject.toString();
        else return null;
    }

    JPlace(JSONObject jsonObject) {
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            parse(jsonObject);
        }
    }

    JPlace() {
        jsonObject = new JSONObject();
        setLatitude(0.0);
        setLongitude(0.0);
        setRadius(-1);
        setMarker(-1);
        setName(null);
        setAddress(null);
        setId(null);
    }

    private void parse(JSONObject jsonObject) {
        if (jsonObject.has(MARKER)) {
            try {
                marker = jsonObject.getInt(MARKER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(RADIUS)) {
            try {
                radius = jsonObject.getInt(RADIUS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(LATITUDE)) {
            try {
                latitude = jsonObject.getDouble(LATITUDE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(LONGITUDE)) {
            try {
                longitude = jsonObject.getDouble(LONGITUDE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(NAME)) {
            try {
                name = jsonObject.getString(NAME);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(ADDRESS)) {
            try {
                address = jsonObject.getString(ADDRESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(ID)) {
            try {
                id = jsonObject.getString(ID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(TYPES)) {
            Type collectionType = new TypeToken<ArrayList<String>>() {
            }.getType();
            try {
                types = new Gson().fromJson(jsonObject.get(TYPES).toString(), collectionType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get current JSON object.
     *
     * @return JSON object
     */
    JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        try {
            jsonObject.put(LATITUDE, latitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        try {
            jsonObject.put(LONGITUDE, longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setMarker(int marker) {
        this.marker = marker;
        try {
            jsonObject.put(MARKER, marker);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setRadius(int radius) {
        this.radius = radius;
        try {
            jsonObject.put(RADIUS, radius);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setName(String name) {
        this.name = name;
        try {
            jsonObject.put(NAME, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setAddress(String address) {
        this.address = address;
        try {
            jsonObject.put(ADDRESS, address);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setId(String id) {
        this.id = id;
        try {
            jsonObject.put(ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTypes() {
        return types;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public int getRadius() {
        return radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getMarker() {
        return marker;
    }
}
