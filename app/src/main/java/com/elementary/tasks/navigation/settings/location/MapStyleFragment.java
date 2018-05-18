package com.elementary.tasks.navigation.settings.location;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.views.roboto.RoboRadioButton;
import com.elementary.tasks.databinding.FragmentSettingMapStyleBinding;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;

/**
 * Copyright 2018 Nazar Suhovich
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
public class MapStyleFragment extends BaseSettingsFragment {

    private FragmentSettingMapStyleBinding binding;

    public static MapStyleFragment newInstance() {
        return new MapStyleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingMapStyleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.styleDay.setOnClickListener(this::invoke);
        binding.styleAubergine.setOnClickListener(this::invoke);
        binding.styleAuto.setOnClickListener(this::invoke);
        binding.styleDark.setOnClickListener(this::invoke);
        binding.styleNight.setOnClickListener(this::invoke);
        binding.styleRetro.setOnClickListener(this::invoke);
        binding.styleSilver.setOnClickListener(this::invoke);

        binding.styleDay.callOnClick();

        selectCurrent(Prefs.getInstance(getContext()).getMapStyle());
    }

    private void selectCurrent(int mapStyle) {
        switch (mapStyle) {
            case 0:
                binding.styleDay.callOnClick();
                break;
            case 1:
                binding.styleRetro.callOnClick();
                break;
            case 2:
                binding.styleSilver.callOnClick();
                break;
            case 3:
                binding.styleNight.callOnClick();
                break;
            case 4:
                binding.styleDark.callOnClick();
                break;
            case 5:
                binding.styleAubergine.callOnClick();
                break;
            case 6:
                binding.styleAuto.callOnClick();
                break;
        }
    }

    private void invoke(View v) {
        clearChecks();
        if (v instanceof RoboRadioButton) {
            ((RoboRadioButton) v).setChecked(true);
        }
    }

    private void clearChecks() {
        binding.styleDay.setChecked(false);
        binding.styleAubergine.setChecked(false);
        binding.styleAuto.setChecked(false);
        binding.styleDark.setChecked(false);
        binding.styleNight.setChecked(false);
        binding.styleRetro.setChecked(false);
        binding.styleSilver.setChecked(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Prefs.getInstance(getContext()).setMapStyle(getSelection());
    }

    private int getSelection() {
        if (binding.styleAuto.isChecked()) {
            return 6;
        } else if (binding.styleDay.isChecked()) {
            return 0;
        } else if (binding.styleRetro.isChecked()) {
            return 1;
        } else if (binding.styleSilver.isChecked()) {
            return 2;
        } else if (binding.styleNight.isChecked()) {
            return 3;
        } else if (binding.styleDark.isChecked()) {
            return 4;
        } else if (binding.styleAubergine.isChecked()) {
            return 5;
        }
        return 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.map_style));
            getCallback().onFragmentSelect(this);
        }
    }
}
