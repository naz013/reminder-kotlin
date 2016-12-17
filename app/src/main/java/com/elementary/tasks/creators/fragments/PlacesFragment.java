package com.elementary.tasks.creators.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
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
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
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
            if (mInterface.getReminder() != null) {
                Reminder item = mInterface.getReminder();
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
            mInterface.setFullScreenMode(isFull);
        }

        @Override
        public void onBackClick() {
            mInterface.setFullScreenMode(false);
        }
    };

    @Override
    protected void recreateMarker() {
        if (placesMap != null) placesMap.recreateMarker(radius);
    }

    @Override
    public boolean save() {
        super.save();
        if (mInterface == null) return false;
        Reminder reminder = mInterface.getReminder();
        int type = Reminder.BY_PLACES;
        if (TextUtils.isEmpty(mInterface.getSummary())) {
            mInterface.showSnackbar(getString(R.string.task_summary_is_empty));
            return false;
        }
        List<Place> places = placesMap.getPlaces();
        if (places.size() == 0) {
            mInterface.showSnackbar(getString(R.string.you_dont_select_place));
            return false;
        }
        if (reminder == null) {
            reminder = new Reminder();
        }
        reminder.setPlaces(places);
        reminder.setTarget(null);
        reminder.setType(type);
        reminder.setExportToCalendar(false);
        reminder.setExportToTasks(false);
        fillExtraData(reminder);
        Log.d(TAG, "save: " + type);
        reminder.setEventTime(null);
        reminder.setStartTime(null);
        Log.d(TAG, "REC_TIME " + TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        RealmDb.getInstance().saveObject(reminder);
//        new AlarmReceiver().enableReminder(mContext, reminder.getUuId());
        return true;
    }

    private void fillExtraData(Reminder reminder) {
        reminder.setSummary(mInterface.getSummary());
        reminder.setGroupUuId(mInterface.getGroup());
        reminder.setRepeatLimit(mInterface.getRepeatLimit());
        reminder.setColor(mInterface.getLedColor());
        reminder.setMelodyPath(mInterface.getMelodyPath());
        reminder.setVolume(mInterface.getVolume());
        reminder.setAuto(mInterface.getAuto());
        reminder.setActive(true);
        reminder.setRemoved(false);
        reminder.setVibrate(mInterface.getVibration());
        reminder.setNotifyByVoice(mInterface.getVoice());
        reminder.setRepeatNotification(mInterface.getNotificationRepeat());
        reminder.setUseGlobal(mInterface.getUseGlobal());
        reminder.setUnlock(mInterface.getUnlock());
        reminder.setAwake(mInterface.getWake());
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
