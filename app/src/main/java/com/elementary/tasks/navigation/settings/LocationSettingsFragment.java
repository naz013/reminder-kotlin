package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.DialogTrackingSettingsLayoutBinding;
import com.elementary.tasks.databinding.FragmentSettingsLocationBinding;
import com.elementary.tasks.navigation.settings.location.MapStyleFragment;
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

    private int mItemSelect;

    private FragmentSettingsLocationBinding binding;
    private View.OnClickListener mRadiusClick = view -> showRadiusPickerDialog();
    private View.OnClickListener mNotificationClick = view -> changeNotificationPrefs();
    private View.OnClickListener mMapTypeClick = view -> showMapTypeDialog();
    private View.OnClickListener mStyleClick = view -> replaceFragment(new MarkerStyleFragment(), getString(R.string.style_of_marker));
    private View.OnClickListener mTrackClick = view -> showTrackerOptionsDialog();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsLocationBinding.inflate(inflater, container, false);
        initMapTypePrefs();
        initMarkerStylePrefs();
        binding.trackerPrefs.setOnClickListener(mTrackClick);
        binding.notificationOptionPrefs.setOnClickListener(mNotificationClick);
        binding.notificationOptionPrefs.setChecked(getPrefs().isDistanceNotificationEnabled());
        initRadiusPrefs();
        return binding.getRoot();
    }

    private void initMapStylePrefs() {
        binding.mapStylePrefs.setOnClickListener(v -> openMapStylesFragment());
        binding.mapStylePrefs.setDetailText(getString(ThemeUtil.getInstance(getContext()).getStyleName()));
        binding.mapStylePrefs.setViewResource(ThemeUtil.getInstance(getContext()).getMapStylePreview());
        binding.mapStylePrefs.setEnabled(Prefs.getInstance(getContext()).getMapType() == 3);
    }

    private void openMapStylesFragment() {
        if (getCallback() != null) {
            getCallback().replaceFragment(MapStyleFragment.newInstance(), getString(R.string.map_style));
        }
    }

    private void initMarkerStylePrefs() {
        binding.markerStylePrefs.setOnClickListener(mStyleClick);
        showMarkerStyle();
    }

    private void showMarkerStyle() {
        binding.markerStylePrefs.setViewResource(ThemeUtil.getInstance(getContext()).getMarkerStyle());
    }

    private void initMapTypePrefs() {
        binding.mapTypePrefs.setOnClickListener(mMapTypeClick);
        showMapType();
    }

    private void showMapType() {
        String[] types = getResources().getStringArray(R.array.map_types);
        binding.mapTypePrefs.setDetailText(types[getPosition(getPrefs().getMapType())]);
    }

    private void initRadiusPrefs() {
        binding.radiusPrefs.setOnClickListener(mRadiusClick);
        showRadius();
    }

    @Override
    public void onResume() {
        super.onResume();
        showMarkerStyle();
        initMapStylePrefs();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.location));
            getCallback().onFragmentSelect(this);
        }
    }

    private void showTrackerOptionsDialog(){
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(R.string.tracking_settings);
        DialogTrackingSettingsLayoutBinding b = DialogTrackingSettingsLayoutBinding.inflate(LayoutInflater.from(getContext()));
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
        int distance = getPrefs().getTrackDistance() - 1;
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
        int time = getPrefs().getTrackTime() - 1;
        b.timeBar.setProgress(time);
        b.timeTitle.setText(String.format(Locale.getDefault(), getString(R.string.x_seconds), String.valueOf(time + 1)));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            getPrefs().setTrackDistance(b.distanceBar.getProgress() + 1);
            getPrefs().setTrackTime(b.timeBar.getProgress() + 1);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showMapTypeDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.map_type));
        String[] types = new String[]{
                getString(R.string.normal),
                getString(R.string.satellite),
                getString(R.string.terrain),
                getString(R.string.hybrid)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_single_choice, types);
        int type = getPrefs().getMapType();
        mItemSelect = getPosition(type);
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> mItemSelect = which);
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
            getPrefs().setMapType(mItemSelect + 1);
            showMapType();
            initMapStylePrefs();
            dialogInterface.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private int getPosition(int type) {
        int mItemSelect;
        if (type == Constants.MAP_SATELLITE){
            mItemSelect = 1;
        } else if (type == Constants.MAP_TERRAIN){
            mItemSelect = 2;
        } else if (type == Constants.MAP_HYBRID){
            mItemSelect = 3;
        } else {
            mItemSelect = 0;
        }
        return mItemSelect;
    }

    private void changeNotificationPrefs() {
        boolean isChecked = binding.notificationOptionPrefs.isChecked();
        binding.notificationOptionPrefs.setChecked(!isChecked);
        getPrefs().setDistanceNotificationEnabled(!isChecked);
    }

    private void showRadius() {
        binding.radiusPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.radius_x_meters),
                String.valueOf(getPrefs().getRadius())));
    }

    private void showRadiusPickerDialog(){
        int radius = getPrefs().getRadius();
        Dialogues.showRadiusDialog(getContext(), radius, new Dialogues.OnValueSelectedListener<Integer>() {
            @Override
            public void onSelected(Integer integer) {
                getPrefs().setRadius(integer);
                showRadius();
            }

            @Override
            public String getTitle(Integer integer) {
                return String.format(Locale.getDefault(), getString(R.string.radius_x_meters),
                        String.valueOf(integer));
            }
        });
    }
}
