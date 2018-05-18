package com.elementary.tasks.creators.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.fragments.PlacesMapFragment;
import com.elementary.tasks.core.interfaces.MapCallback;
import com.elementary.tasks.core.interfaces.MapListener;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.FragmentReminderPlaceBinding;
import com.elementary.tasks.reminder.models.Place;
import com.elementary.tasks.reminder.models.Reminder;
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

public class PlacesFragment extends RadiusTypeFragment {

    private static final String TAG = "PlacesFragment";

    private PlacesMapFragment placesMap;

    private MapCallback mCallback = new MapCallback() {
        @Override
        public void onMapReady() {
            if (getInterface().getReminder() != null) {
                Reminder item = getInterface().getReminder();
                placesMap.selectMarkers(item.getPlaces());
            }
        }
    };
    private MapListener mListener = new MapListener() {
        @Override
        public void placeChanged(LatLng place, String address) {

        }

        @Override
        public void onZoomClick(boolean isFull) {
            getInterface().setFullScreenMode(isFull);
        }

        @Override
        public void onBackClick() {
            getInterface().setFullScreenMode(false);
        }
    };

    @Override
    protected void recreateMarker() {
        if (placesMap != null) {
            placesMap.recreateMarker(radius);
        }
    }

    @Override
    public Reminder prepare() {
        if (super.prepare() == null) return null;
        if (getInterface() == null) return null;
        Reminder reminder = getInterface().getReminder();
        int type = Reminder.BY_PLACES;
        if (TextUtils.isEmpty(getInterface().getSummary())) {
            getInterface().showSnackbar(getString(R.string.task_summary_is_empty));
            return null;
        }
        List<Place> places = placesMap.getPlaces();
        if (places.size() == 0) {
            getInterface().showSnackbar(getString(R.string.you_dont_select_place));
            return null;
        }
        if (reminder == null) {
            reminder = new Reminder();
        }
        reminder.setPlaces(places);
        reminder.setTarget(null);
        reminder.setType(type);
        reminder.setExportToCalendar(false);
        reminder.setExportToTasks(false);
        reminder.setClear(getInterface());
        reminder.setEventTime(null);
        reminder.setStartTime(null);
        LogUtil.d(TAG, "REC_TIME " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true, true));
        return reminder;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_location_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_custom_radius:
                showRadiusPickerDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentReminderPlaceBinding binding = FragmentReminderPlaceBinding.inflate(inflater, container, false);
        Prefs prefs = Prefs.getInstance(getActivity());
        placesMap = new PlacesMapFragment();
        placesMap.setListener(mListener);
        placesMap.setCallback(mCallback);
        placesMap.setRadius(prefs.getRadius());
        placesMap.setMarkerStyle(prefs.getMarkerStyle());
        getFragmentManager().beginTransaction()
                .replace(binding.mapPlace.getId(), placesMap)
                .addToBackStack(null)
                .commit();
        return binding.getRoot();
    }

    @Override
    public boolean onBackPressed() {
        return placesMap == null || placesMap.onBackPressed();
    }
}
