package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.views.PrefsView;
import com.elementary.tasks.databinding.DialogTrackingSettingsLayoutBinding;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;
import com.elementary.tasks.databinding.FragmentSettingsLocationBinding;
import com.elementary.tasks.navigation.settings.location.MarkerStyleFragment;

import java.util.Locale;

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

public class LocationSettingsFragment extends BaseSettingsFragment {

    private PrefsView mRadiusPrefs;
    private PrefsView mNotificationPrefs;
    private View.OnClickListener mRadiusClick = view -> showRadiusPickerDialog();
    private View.OnClickListener mNotificationClick = view -> changeNotificationPrefs();
    private View.OnClickListener mMapTypeClick = view -> showMapTypeDialog();
    private View.OnClickListener mStyleClick = view -> replaceFragment(new MarkerStyleFragment(), getString(R.string.style_of_marker));
    private View.OnClickListener mTrackClick = view -> showTrackerOptionsDialog();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentSettingsLocationBinding binding = FragmentSettingsLocationBinding.inflate(inflater, container, false);
        binding.mapTypePrefs.setOnClickListener(mMapTypeClick);
        binding.markerStylePrefs.setOnClickListener(mStyleClick);
        binding.trackerPrefs.setOnClickListener(mTrackClick);
        mNotificationPrefs = binding.notificationOptionPrefs;
        mNotificationPrefs.setOnClickListener(mNotificationClick);
        mNotificationPrefs.setChecked(Prefs.getInstance(mContext).isDistanceNotificationEnabled());
        mRadiusPrefs = binding.radiusPrefs;
        mRadiusPrefs.setOnClickListener(mRadiusClick);
        showRadius();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.location));
            mCallback.onFragmentSelect(this);
        }
    }

    private void showTrackerOptionsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.tracking_settings);
        DialogTrackingSettingsLayoutBinding b = DialogTrackingSettingsLayoutBinding.inflate(LayoutInflater.from(mContext));
        b.distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.distanceTitle.setText(String.format(Locale.getDefault(), getString(R.string.x_meters), String.valueOf(progress + 1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int distance = Prefs.getInstance(mContext).getTrackDistance() - 1;
        b.distanceBar.setProgress(distance);
        b.distanceTitle.setText(String.format(Locale.getDefault(), getString(R.string.x_meters), String.valueOf(distance + 1)));
        b.timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.timeTitle.setText(String.format(Locale.getDefault(), getString(R.string.x_seconds), String.valueOf(progress + 1)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int time = Prefs.getInstance(mContext).getTrackTime() - 1;
        b.timeBar.setProgress(time);
        b.timeTitle.setText(String.format(Locale.getDefault(), getString(R.string.x_seconds), String.valueOf(time + 1)));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            Prefs.getInstance(mContext).setTrackDistance(b.distanceBar.getProgress() + 1);
            Prefs.getInstance(mContext).setTrackTime(b.timeBar.getProgress() + 1);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showMapTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.map_type));
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.map_types,
                android.R.layout.simple_list_item_single_choice);
        int type = Prefs.getInstance(mContext).getMapType();
        int position;
        if (type == Constants.MAP_NORMAL){
            position = 0;
        } else if (type == Constants.MAP_SATELLITE){
            position = 1;
        } else if (type == Constants.MAP_TERRAIN){
            position = 2;
        } else if (type == Constants.MAP_HYBRID){
            position = 3;
        } else {
            position = 0;
        }
        builder.setSingleChoiceItems(adapter, position, (dialog, which) -> {
            if (which != -1) {
                Prefs.getInstance(mContext).setMapType(which + 1);
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void changeNotificationPrefs() {
        if (mNotificationPrefs.isChecked()) {
            mNotificationPrefs.setChecked(false);
            Prefs.getInstance(mContext).setDistanceNotificationEnabled(false);
        } else {
            mNotificationPrefs.setChecked(true);
            Prefs.getInstance(mContext).setDistanceNotificationEnabled(true);
        }
    }

    private void showRadius() {
        mRadiusPrefs.setValue(Prefs.getInstance(mContext).getRadius());
    }

    private void showRadiusPickerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.radius);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(mContext));
        b.seekBar.setMax(5000);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.format(Locale.getDefault(), getString(R.string.radius_x_meters), String.valueOf(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int radius = Prefs.getInstance(mContext).getRadius();
        b.seekBar.setProgress(radius);
        b.titleView.setText(String.format(Locale.getDefault(), getString(R.string.radius_x_meters), String.valueOf(radius)));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            Prefs.getInstance(mContext).setRadius(b.seekBar.getProgress());
            showRadius();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
