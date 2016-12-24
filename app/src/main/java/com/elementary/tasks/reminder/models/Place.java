package com.elementary.tasks.reminder.models;

import com.elementary.tasks.core.utils.SuperUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

public class Place {

    private int radius;
    private int marker;
    private double latitude;
    private double longitude;
    private String name;
    private String id;
    private String address;
    private List<String> tags;

    public Place(RealmPlace2 item) {
        this.radius = item.getRadius();
        this.marker = item.getMarker();
        this.latitude = item.getLatitude();
        this.longitude = item.getLongitude();
        this.name = item.getName();
        this.id = item.getId();
        this.address = item.getAddress();
        this.tags = wrapStringArray(item.getTags());
    }

    public Place(int radius, int marker, double latitude, double longitude, String name, String address, List<String> tags) {
        this.radius = radius;
        this.marker = marker;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.id = UUID.randomUUID().toString();
        this.address = address;
        this.tags = tags;
    }

    private List<String> wrapStringArray(List<RealmString> list) {
        List<String> strings = new ArrayList<>();
        if (list != null) {
            for (RealmString string : list) {
                strings.add(string.getString());
            }
        }
        return strings;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    @Override
    public String toString(){
        return SuperUtil.getObjectPrint(this, Place.class);
    }
}