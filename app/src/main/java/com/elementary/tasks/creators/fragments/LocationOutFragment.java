package com.elementary.tasks.creators.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.elementary.tasks.R;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlImpl;
import com.elementary.tasks.core.fragments.MapFragment;
import com.elementary.tasks.core.interfaces.MapCallback;
import com.elementary.tasks.core.interfaces.MapListener;
import com.elementary.tasks.core.location.LocationTracker;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.ActionView;
import com.elementary.tasks.databinding.FragmentReminderLocationOutBinding;
import com.elementary.tasks.reminder.models.Place;
import com.elementary.tasks.reminder.models.Reminder;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
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

public class LocationOutFragment extends RadiusTypeFragment {

    private static final String TAG = "DateFragment";
    private static final int CONTACTS = 132;
    private static final int LOCATION = 202;
    public static final int CONTACTS_ACTION = 133;

    private FragmentReminderLocationOutBinding binding;
    private MapFragment mapFragment;

    private LocationTracker mTracker;
    private LatLng lastPos;

    private ActionView.OnActionListener mActionListener = new ActionView.OnActionListener() {
        @Override
        public void onActionChange(boolean hasAction) {
            if (!hasAction) {
                mInterface.setEventHint(getString(R.string.remind_me));
                mInterface.setHasAutoExtra(false, null);
            }
        }

        @Override
        public void onTypeChange(boolean isMessageType) {
            if (isMessageType) {
                mInterface.setEventHint(getString(R.string.message));
                mInterface.setHasAutoExtra(true, getString(R.string.enable_sending_sms_automatically));
            } else {
                mInterface.setEventHint(getString(R.string.remind_me));
                mInterface.setHasAutoExtra(true, getString(R.string.enable_making_phone_calls_automatically));
            }
        }
    };
    private MapCallback mCallback = new MapCallback() {
        @Override
        public void onMapReady() {
            if (mInterface.getReminder() != null) {
                Reminder item = mInterface.getReminder();
                String text = item.getSummary();
                Place jPlace = item.getPlaces().get(0);
                double latitude = jPlace.getLatitude();
                double longitude = jPlace.getLongitude();
                radius = jPlace.getRadius();
                if (mapFragment != null) {
                    mapFragment.setMarkerRadius(radius);
                    lastPos = new LatLng(latitude, longitude);
                    mapFragment.addMarker(lastPos, text, true, true, radius);
//                    toggleMap();
                }
                binding.mapLocation.setText(SuperUtil.getAddress(lastPos.latitude, lastPos.longitude));
                binding.mapCheck.setChecked(true);
            }
        }
    };
    private MapListener mListener = new MapListener() {
        @Override
        public void placeChanged(LatLng place, String address) {
            lastPos = place;
            String location = SuperUtil.getAddress(place.latitude, place.longitude);
            binding.currentLocation.setText(location);
        }

        @Override
        public void onZoomClick(boolean isFull) {
            mInterface.setFullScreenMode(isFull);
        }

        @Override
        public void onBackClick() {
            if (mapFragment.isFullscreen()) {
                mapFragment.setFullscreen(false);
                mInterface.setFullScreenMode(true);
            }
            ViewUtils.fadeOutAnimation(binding.mapContainer);
            ViewUtils.fadeInAnimation(binding.specsContainer);
        }
    };
    private LocationTracker.Callback mLocationCallback = new LocationTracker.Callback() {
        @Override
        public void onChange(double lat, double lon) {
            lastPos = new LatLng(lat, lon);
            String _Location = SuperUtil.getAddress(lat, lon);
            String text = mInterface.getSummary();
            if (TextUtils.isEmpty(text)) text = _Location;
            binding.currentLocation.setText(_Location);
            if (mapFragment != null) {
                mapFragment.addMarker(lastPos, text, true, true, radius);
            }
        }
    };

    @Override
    protected void recreateMarker() {
        mapFragment.recreateMarker(radius);
    }

    @Override
    public boolean save() {
        super.save();
        if (mInterface == null) return false;
        Reminder reminder = mInterface.getReminder();
        int type = Reminder.BY_OUT;
        boolean isAction = binding.actionView.hasAction();
        if (TextUtils.isEmpty(mInterface.getSummary()) && !isAction) {
            mInterface.showSnackbar(getString(R.string.task_summary_is_empty));
            return false;
        }
        if (lastPos == null) {
            mInterface.showSnackbar(getString(R.string.you_dont_select_place));
            return false;
        }
        String number = null;
        if (isAction) {
            number = binding.actionView.getNumber();
            if (TextUtils.isEmpty(number)) {
                mInterface.showSnackbar(getString(R.string.you_dont_insert_number));
                return false;
            }
            if (binding.actionView.getType() == ActionView.TYPE_CALL) {
                type = Reminder.BY_OUT_CALL;
            } else {
                type = Reminder.BY_OUT_SMS;
            }
        }
        if (reminder == null) {
            reminder = new Reminder();
        }
        Place place = new Place(radius, mapFragment.getMarkerStyle(), lastPos.latitude, lastPos.longitude, mInterface.getSummary(), number, null);
        List<Place> places = Collections.singletonList(place);
        reminder.setPlaces(places);
        reminder.setTarget(number);
        reminder.setType(type);
        reminder.setExportToCalendar(false);
        reminder.setExportToTasks(false);
        reminder.setClear(mInterface);
        if (binding.attackDelay.isChecked()) {
            long startTime = binding.dateView.getDateTime();
            reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
            reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
            LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true));
        } else {
            reminder.setEventTime(null);
            reminder.setStartTime(null);
        }
        RealmDb.getInstance().saveObject(reminder);
        EventControl control = EventControlImpl.getController(mContext, reminder);
        control.start();
        return true;
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
        binding = FragmentReminderLocationOutBinding.inflate(inflater, container, false);
        mapFragment = MapFragment.newInstance(true, true, true, true,
                Prefs.getInstance(mContext).getMarkerStyle(), ThemeUtil.getInstance(mContext).isDark());
        mapFragment.setListener(mListener);
        mapFragment.setCallback(mCallback);
        getFragmentManager().beginTransaction()
                .replace(binding.mapFrame.getId(), mapFragment)
                .addToBackStack(null)
                .commit();
        binding.actionView.setListener(mActionListener);
        binding.actionView.setActivity(getActivity());
        binding.actionView.setContactClickListener(view -> selectContact());

        binding.delayLayout.setVisibility(View.GONE);
        binding.mapContainer.setVisibility(View.GONE);
        binding.attackDelay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) binding.delayLayout.setVisibility(View.VISIBLE);
            else binding.delayLayout.setVisibility(View.GONE);
        });
        ImageButton mapButton = binding.mapButton;
        if (ThemeUtil.getInstance(mContext).isDark()){
            mapButton.setImageResource(R.drawable.ic_map_white);
        } else {
            mapButton.setImageResource(R.drawable.ic_map);
        }
        mapButton.setOnClickListener(view -> {
            if (binding.mapCheck.isChecked()) {
                toggleMap();
            }
            binding.mapCheck.setChecked(true);
        });
        binding.currentCheck.setOnCheckedChangeListener((compoundButton, b) -> {
            if (binding.currentCheck.isChecked()) {
                binding.mapCheck.setChecked(false);
                if (Permissions.checkPermission(getActivity(), Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
                    mTracker = new LocationTracker(mContext, mLocationCallback);
                } else {
                    Permissions.requestPermission(getActivity(), LOCATION, Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION);
                }
            }
        });
        binding.mapCheck.setOnCheckedChangeListener((compoundButton, b) -> {
            if (binding.mapCheck.isChecked()) {
                binding.currentCheck.setChecked(false);
                toggleMap();
                if (mTracker != null) {
                    mTracker.removeUpdates();
                }
            }
        });
        binding.currentCheck.setChecked(true);
        editReminder();
        return binding.getRoot();
    }

    private void toggleMap() {
        if (binding.mapContainer != null && binding.mapContainer.getVisibility() == View.VISIBLE) {
            ViewUtils.fadeOutAnimation(binding.mapContainer);
            ViewUtils.fadeInAnimation(binding.specsContainer);
        } else {
            ViewUtils.fadeOutAnimation(binding.specsContainer);
            ViewUtils.fadeInAnimation(binding.mapContainer);
            if (mapFragment != null) {
                mapFragment.showShowcase();
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        return mapFragment == null || mapFragment.onBackPressed();
    }

    private void editReminder() {
        if (mInterface.getReminder() == null) return;
        Reminder reminder = mInterface.getReminder();
        if (reminder.getEventTime() != null) {
            binding.dateView.setDateTime(reminder.getEventTime());
            binding.attackDelay.setChecked(true);
        }
        if (reminder.getTarget() != null) {
            binding.actionView.setAction(true);
            binding.actionView.setNumber(reminder.getTarget());
            if (Reminder.isKind(reminder.getType(), Reminder.Kind.CALL)) {
                binding.actionView.setType(ActionView.TYPE_CALL);
            } else if (Reminder.isKind(reminder.getType(), Reminder.Kind.SMS)) {
                binding.actionView.setType(ActionView.TYPE_MESSAGE);
            }
        }
    }

    private void selectContact() {
        if (Permissions.checkPermission(getActivity(), Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(getActivity(), Constants.REQUEST_CODE_CONTACTS);
        } else {
            Permissions.requestPermission(getActivity(), CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            String number = data.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
            binding.actionView.setNumber(number);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mTracker != null) mTracker.removeUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectContact();
                }
                break;
            case LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.currentCheck.setChecked(true);
                }
                break;
            case CONTACTS_ACTION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.actionView.setAction(true);
                }
                break;
        }
    }
}
