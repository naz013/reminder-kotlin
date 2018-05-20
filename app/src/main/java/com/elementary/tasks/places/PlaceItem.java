package com.elementary.tasks.places;

import com.elementary.tasks.core.utils.SuperUtil;
import com.google.gson.annotations.SerializedName;

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

public class PlaceItem {

    @SerializedName("title")
    private String title;
    @SerializedName("key")
    private String key;
    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;
    @SerializedName("icon")
    private int icon;
    @SerializedName("radius")
    private int radius;

    public PlaceItem() {
    }

    public PlaceItem(String title, String key, double lat, double lng, int icon, int radius) {
        this.title = title;
        this.key = key;
        this.lat = lat;
        this.lng = lng;
        this.icon = icon;
        this.radius = radius;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return SuperUtil.getObjectPrint(this, PlaceItem.class);
    }
}
