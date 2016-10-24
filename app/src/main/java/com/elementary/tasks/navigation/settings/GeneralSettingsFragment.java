package com.elementary.tasks.navigation.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.FragmentSettingsGeneralBinding;

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

public class GeneralSettingsFragment extends SettingsFragment {

    private FragmentSettingsGeneralBinding binding;
    private View.OnClickListener mDarkClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (Prefs.getInstance(mContext).isDarkModeEnabled()) {
                Prefs.getInstance(mContext).setDarkModeEnabled(false);
                binding.darkPrefs.setChecked(false);
            } else {
                Prefs.getInstance(mContext).setDarkModeEnabled(true);
                binding.darkPrefs.setChecked(true);
            }
        }
    };
    private View.OnClickListener mDayNightClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (Prefs.getInstance(mContext).isNightModeEnabled()) {
                Prefs.getInstance(mContext).setNightModeEnabled(false);
                binding.dayNightPrefs.setChecked(false);
            } else {
                Prefs.getInstance(mContext).setNightModeEnabled(true);
                binding.dayNightPrefs.setChecked(true);
            }
            checkDayNight();
            getActivity().recreate();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsGeneralBinding.inflate(inflater, container, false);
        binding.themePrefs.setViewResource(ThemeUtil.getInstance(mContext).getIndicator(ThemeUtil.getInstance(mContext).getStyle()));
        binding.darkPrefs.setChecked(Prefs.getInstance(mContext).isDarkModeEnabled());
        binding.darkPrefs.setOnClickListener(mDarkClick);
        binding.dayNightPrefs.setChecked(Prefs.getInstance(mContext).isNightModeEnabled());
        binding.dayNightPrefs.setOnClickListener(mDayNightClick);
        checkDayNight();
        return binding.getRoot();
    }

    private void checkDayNight() {
        if (binding.dayNightPrefs.isChecked()) {
            binding.darkPrefs.setEnabled(false);
        } else {
            binding.darkPrefs.setEnabled(true);
        }
    }
}
