package com.elementary.tasks.navigation.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.FragmentSettingsGeneralBinding;
import com.elementary.tasks.navigation.settings.images.MainImageActivity;
import com.elementary.tasks.navigation.settings.theme.SelectThemeActivity;

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

public class GeneralSettingsFragment extends BaseSettingsFragment {

    private FragmentSettingsGeneralBinding binding;
    private View.OnClickListener mDarkClick = view -> changeNightMode();
    private View.OnClickListener mDayNightClick = view -> changeDayNightMode();
    private View.OnClickListener mFoldingClick = view -> changeSmartFoldMode();
    private View.OnClickListener mWearClick = view -> changeWearNotification();
    private View.OnClickListener mThemeClick = view -> selectTheme();
    private View.OnClickListener mMainImageClick = view -> selectMainImage();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsGeneralBinding.inflate(inflater, container, false);
        initTheme();
        initMainImage();
        initDarkMode();
        initDayNight();
        initSmartFold();
        initWearNotification();
        checkDayNight();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.general));
            mCallback.onFragmentSelect(this);
        }
    }

    private void selectMainImage() {
        startActivity(new Intent(mContext, MainImageActivity.class));
    }

    private void initMainImage() {
        binding.mainImagePrefs.setOnClickListener(mMainImageClick);
    }

    private void selectTheme() {
        startActivity(new Intent(mContext, SelectThemeActivity.class));
    }

    private void initTheme() {
        binding.themePrefs.setViewResource(ThemeUtil.getInstance(mContext).getIndicator(Prefs.getInstance(mContext).getAppTheme()));
        binding.themePrefs.setOnClickListener(mThemeClick);
    }

    private void initDarkMode() {
        binding.darkPrefs.setChecked(Prefs.getInstance(mContext).isDarkModeEnabled());
        binding.darkPrefs.setOnClickListener(mDarkClick);
    }

    private void initDayNight() {
        binding.dayNightPrefs.setChecked(Prefs.getInstance(mContext).isNightModeEnabled());
        binding.dayNightPrefs.setOnClickListener(mDayNightClick);
    }

    private void initSmartFold() {
        binding.smartFoldPrefs.setChecked(Prefs.getInstance(mContext).isFoldingEnabled());
        binding.smartFoldPrefs.setOnClickListener(mFoldingClick);
    }

    private void initWearNotification() {
        binding.wearPrefs.setChecked(Prefs.getInstance(mContext).isWearEnabled());
        binding.wearPrefs.setOnClickListener(mWearClick);
    }

    private void changeWearNotification() {
        if (Prefs.getInstance(mContext).isWearEnabled()) {
            Prefs.getInstance(mContext).setWearEnabled(false);
            binding.wearPrefs.setChecked(false);
        } else {
            Prefs.getInstance(mContext).setWearEnabled(true);
            binding.wearPrefs.setChecked(true);
        }
    }

    private void changeSmartFoldMode() {
        if (Prefs.getInstance(mContext).isFoldingEnabled()) {
            Prefs.getInstance(mContext).setFoldingEnabled(false);
            binding.smartFoldPrefs.setChecked(false);
        } else {
            Prefs.getInstance(mContext).setFoldingEnabled(true);
            binding.smartFoldPrefs.setChecked(true);
        }
    }

    private void changeDayNightMode() {
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

    private void changeNightMode() {
        if (Prefs.getInstance(mContext).isDarkModeEnabled()) {
            Prefs.getInstance(mContext).setDarkModeEnabled(false);
            binding.darkPrefs.setChecked(false);
        } else {
            Prefs.getInstance(mContext).setDarkModeEnabled(true);
            binding.darkPrefs.setChecked(true);
        }
        getActivity().recreate();
    }

    private void checkDayNight() {
        if (binding.dayNightPrefs.isChecked()) {
            binding.darkPrefs.setEnabled(false);
        } else {
            binding.darkPrefs.setEnabled(true);
        }
    }
}
