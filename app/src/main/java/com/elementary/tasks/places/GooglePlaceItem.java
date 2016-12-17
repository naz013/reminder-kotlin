package com.elementary.tasks.places;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

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

public class GooglePlaceItem {
    private String name, id, icon, address;
    private LatLng position;
    private boolean selected;
    private List<String> types;

    public GooglePlaceItem() {
        selected = false;
    }

    public GooglePlaceItem(String name, String id, String icon, String address,
                           LatLng position, List<String> types, boolean selected){
        this.name = name;
        this.id = id;
        this.icon = icon;
        this.address = address;
        this.position = position;
        this.types = types;
        this.selected = selected;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getAddress() {
        return address;
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }
}
