package com.elementary.tasks.navigation.settings;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.databinding.FragmentSettingsBinding;

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

public class SettingsFragment extends BaseSettingsFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentSettingsBinding binding = FragmentSettingsBinding.inflate(inflater, container, false);
        binding.generalSettings.setOnClickListener(view -> replaceFragment(new GeneralSettingsFragment(), getString(R.string.general)));
        binding.otherSettings.setOnClickListener(view -> replaceFragment(new OtherSettingsFragment(), getString(R.string.other)));
        binding.voiceSettings.setOnClickListener(view -> replaceFragment(new VoiceSettingsFragment(), getString(R.string.voice_control)));
        binding.notesSettings.setOnClickListener(view -> replaceFragment(new NoteSettingsFragment(), getString(R.string.notes)));
        binding.locationSettings.setOnClickListener(view -> replaceFragment(new LocationSettingsFragment(), getString(R.string.location)));
        binding.additionalSettings.setOnClickListener(view -> replaceFragment(new AdditionalSettingsFragment(), getString(R.string.additional)));
        binding.notificationSettings.setOnClickListener(view -> replaceFragment(new NotificationSettingsFragment(), getString(R.string.notification)));
        binding.exportSettings.setOnClickListener(view -> replaceFragment(new ExportSettingsFragment(), getString(R.string.export_and_sync)));
        binding.calendarSettings.setOnClickListener(view -> replaceFragment(new CalendarSettingsFragment(), getString(R.string.calendar)));
        binding.birthdaysSettings.setOnClickListener(view -> replaceFragment(new BirthdaySettingsFragment(), getString(R.string.birthdays)));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.action_settings));
            getCallback().onFragmentSelect(this);
            getCallback().setClick(null);
        }
    }
}
