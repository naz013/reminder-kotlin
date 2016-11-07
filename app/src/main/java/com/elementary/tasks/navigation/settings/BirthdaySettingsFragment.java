package com.elementary.tasks.navigation.settings;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.services.BirthdayAlarm;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.databinding.FragmentBirthdaysSettingsBinding;

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

public class BirthdaySettingsFragment extends BaseSettingsFragment {

    private FragmentBirthdaysSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBirthdaysSettingsBinding.inflate(inflater, container, false);
        initBirthdayReminderPrefs();
        initBirthdaysWidgetPrefs();
        initPermanentPrefs();
        return binding.getRoot();
    }

    private void initPermanentPrefs() {
        binding.birthdayPermanentPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayPermanentEnabled());
        binding.birthdayPermanentPrefs.setOnClickListener(view -> changeBirthdayPermanentPrefs());
    }

    private void changeBirthdayPermanentPrefs() {
        boolean isChecked = binding.birthdayPermanentPrefs.isChecked();
        binding.birthdayPermanentPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayPermanentEnabled(!isChecked);
        // TODO: 07.11.2016 Update permanent notification
    }

    private void initBirthdaysWidgetPrefs() {
        binding.widgetShowPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayInWidgetEnabled());
        binding.widgetShowPrefs.setOnClickListener(view -> changeWidgetPrefs());
    }

    private void changeWidgetPrefs() {
        boolean isChecked = binding.widgetShowPrefs.isChecked();
        binding.widgetShowPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBirthdayInWidgetEnabled(!isChecked);
        //// TODO: 07.11.2016 Update widget
    }

    private void initBirthdayReminderPrefs() {
        binding.birthReminderPrefs.setOnClickListener(view -> changeBirthdayPrefs());
        binding.birthReminderPrefs.setChecked(Prefs.getInstance(mContext).isBirthdayReminderEnabled());
    }

    private void changeBirthdayPrefs() {
        boolean isChecked = !binding.birthReminderPrefs.isChecked();
        binding.birthReminderPrefs.setChecked(isChecked);
        Prefs.getInstance(mContext).setBirthdayReminderEnabled(isChecked);
        if (isChecked) {
            new BirthdayAlarm().setAlarm(mContext);
        } else {
            cleanBirthdays();
            new BirthdayAlarm().cancelAlarm(mContext);
        }
    }

    private void cleanBirthdays(){
        new Thread(() -> {
            Looper.prepare();
            // TODO: 07.11.2016 Remove all birthdays
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.birthdays));
            mCallback.onFragmentSelect(this);
        }
    }
}
