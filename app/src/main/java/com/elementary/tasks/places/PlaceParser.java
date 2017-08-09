package com.elementary.tasks.places;

import com.elementary.tasks.core.network.places.Location;
import com.elementary.tasks.core.network.places.Place;
import com.google.android.gms.maps.model.LatLng;

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

    public static GooglePlaceItem getDetails(Place place) {
        GooglePlaceItem model = new GooglePlaceItem();
        model.setName(place.getName());
        model.setId(place.getId());
        model.setIcon(place.getIcon());
        model.setAddress(place.getFormattedAddress());
        model.setPosition(getCoordinates(place.getGeometry().getLocation()));
        model.setTypes(place.getTypes());
        return model;
    }

    private static LatLng getCoordinates(Location location) {
        if (location != null) {
            return new LatLng(location.getLat(), location.getLng());
        }
        return null;
    }
}
