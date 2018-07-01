package com.elementary.tasks.reminder.create_edit.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.fragments.AdvancedMapFragment;
import com.elementary.tasks.core.interfaces.MapCallback;
import com.elementary.tasks.core.interfaces.MapListener;
import com.elementary.tasks.core.location.LocationTracker;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.ActionView;
import com.elementary.tasks.databinding.FragmentReminderLocationOutBinding;
import com.elementary.tasks.core.data.models.Place;
import com.elementary.tasks.core.data.models.Reminder;
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
    private AdvancedMapFragment advancedMapFragment;

    private LocationTracker mTracker;
    private LatLng lastPos;

    private ActionView.OnActionListener mActionListener = new ActionView.OnActionListener() {
        @Override
        public void onActionChange(boolean hasAction) {
            if (!hasAction) {
                getInterface().setEventHint(getString(R.string.remind_me));
                getInterface().setHasAutoExtra(false, null);
            }
        }

        @Override
        public void onTypeChange(boolean isMessageType) {
            if (isMessageType) {
                getInterface().setEventHint(getString(R.string.message));
                getInterface().setHasAutoExtra(true, getString(R.string.enable_sending_sms_automatically));
            } else {
                getInterface().setEventHint(getString(R.string.remind_me));
                getInterface().setHasAutoExtra(true, getString(R.string.enable_making_phone_calls_automatically));
            }
        }
    };
    private MapCallback mCallback = this::showPlaceOnMap;

    private void showPlaceOnMap() {
        if (getInterface().getReminder() != null) {
            Reminder item = getInterface().getReminder();
            if (!Reminder.isGpsType(item.getType())) return;
            String text = item.getSummary();
            Place jPlace = item.getPlaces().get(0);
            double latitude = jPlace.getLatitude();
            double longitude = jPlace.getLongitude();
            radius = jPlace.getRadius();
            if (advancedMapFragment != null) {
                advancedMapFragment.setMarkerRadius(radius);
                lastPos = new LatLng(latitude, longitude);
                advancedMapFragment.addMarker(lastPos, text, true, true, radius);
            }
            binding.mapLocation.setText(SuperUtil.getAddress(lastPos.latitude, lastPos.longitude));
            binding.mapCheck.setChecked(true);
        }
    }

    private MapListener mListener = new MapListener() {
        @Override
        public void placeChanged(LatLng place, String address) {
            lastPos = place;
            String location = SuperUtil.getAddress(place.latitude, place.longitude);
            binding.currentLocation.setText(location);
        }

        @Override
        public void onZoomClick(boolean isFull) {
            getInterface().setFullScreenMode(isFull);
        }

        @Override
        public void onBackClick() {
            if (advancedMapFragment.isFullscreen()) {
                advancedMapFragment.setFullscreen(false);
                getInterface().setFullScreenMode(false);
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
            String text = getInterface().getSummary();
            if (TextUtils.isEmpty(text)) text = _Location;
            binding.currentLocation.setText(_Location);
            if (advancedMapFragment != null) {
                advancedMapFragment.addMarker(lastPos, text, true, true, radius);
            }
        }
    };

    @Override
    protected void recreateMarker() {
        advancedMapFragment.recreateMarker(radius);
    }

    @Override
    public Reminder prepare() {
        if (super.prepare() == null) return null;
        if (getInterface() == null) return null;
        Reminder reminder = getInterface().getReminder();
        int type = Reminder.BY_OUT;
        boolean isAction = binding.actionView.hasAction();
        if (TextUtils.isEmpty(getInterface().getSummary()) && !isAction) {
            getInterface().showSnackbar(getString(R.string.task_summary_is_empty));
            return null;
        }
        if (lastPos == null) {
            getInterface().showSnackbar(getString(R.string.you_dont_select_place));
            return null;
        }
        String number = null;
        if (isAction) {
            number = binding.actionView.getNumber();
            if (TextUtils.isEmpty(number)) {
                getInterface().showSnackbar(getString(R.string.you_dont_insert_number));
                return null;
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
        Place place = new Place(radius, advancedMapFragment.getMarkerStyle(), lastPos.latitude, lastPos.longitude, getInterface().getSummary(), number, null);
        List<Place> places = Collections.singletonList(place);
        reminder.setPlaces(places);
        reminder.setTarget(number);
        reminder.setType(type);
        reminder.setExportToCalendar(false);
        reminder.setExportToTasks(false);
        reminder.setClear(getInterface());
        if (binding.attackDelay.isChecked()) {
            long startTime = binding.dateView.getDateTime();
            reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
            reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
            LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true));
        } else {
            reminder.setEventTime(null);
            reminder.setStartTime(null);
        }
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReminderLocationOutBinding.inflate(inflater, container, false);
        advancedMapFragment = AdvancedMapFragment.newInstance(true, true, true, true,
                Prefs.getInstance(getContext()).getMarkerStyle(), ThemeUtil.getInstance(getContext()).isDark());
        advancedMapFragment.setListener(mListener);
        advancedMapFragment.setCallback(mCallback);
        getFragmentManager().beginTransaction()
                .replace(binding.mapFrame.getId(), advancedMapFragment)
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
        binding.mapButton.setOnClickListener(view -> {
            if (binding.mapCheck.isChecked()) {
                toggleMap();
            }
            binding.mapCheck.setChecked(true);
        });
        binding.currentCheck.setOnCheckedChangeListener((compoundButton, b) -> {
            if (binding.currentCheck.isChecked()) {
                binding.mapCheck.setChecked(false);
                if (Permissions.checkPermission(getActivity(), Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
                    mTracker = new LocationTracker(getContext(), mLocationCallback);
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
            if (advancedMapFragment != null) {
                advancedMapFragment.showShowcase();
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        return advancedMapFragment == null || advancedMapFragment.onBackPressed();
    }

    private void editReminder() {
        if (getInterface().getReminder() == null) return;
        Reminder reminder = getInterface().getReminder();
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
        binding.actionView.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) return;
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
