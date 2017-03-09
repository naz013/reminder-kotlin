package com.elementary.tasks.reminder.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

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

public class RealmPlace2 extends RealmObject {

    @SerializedName("radius")
    private int radius;
    @SerializedName("marker")
    private int marker;
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("name")
    private String name;
    @SerializedName("id")
    @PrimaryKey
    private String id;
    @SerializedName("address")
    private String address;
    @SerializedName("tags")
    private RealmList<RealmString> tags = new RealmList<>();

    public RealmPlace2() {}

    public RealmPlace2(Place item) {
        this.radius = item.getRadius();
        this.marker = item.getMarker();
        this.latitude = item.getLatitude();
        this.longitude = item.getLongitude();
        this.name = item.getName();
        this.id = item.getId();
        this.address = item.getAddress();
        this.tags = wrapStringArray(item.getTags());
    }

    private RealmList<RealmString> wrapStringArray(List<String> list) {
        RealmList<RealmString> strings = new RealmList<>();
        if (list != null) {
            for (String string : list) {
                strings.add(new RealmString(string));
            }
        }
        return strings;
    }

    public void setTags(List<String> tags) {
        this.tags = wrapStringArray(tags);
    }

    public RealmList<RealmString> getTags() {
        return tags;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getMarker() {
        return marker;
    }

    public void setMarker(int marker) {
        this.marker = marker;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}