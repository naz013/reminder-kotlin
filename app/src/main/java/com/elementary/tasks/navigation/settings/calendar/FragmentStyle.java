package com.elementary.tasks.navigation.settings.calendar;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.views.ColorPickerView;
import com.elementary.tasks.databinding.FragmentCalendarStyleBinding;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;

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

public abstract class FragmentStyle extends BaseSettingsFragment implements ColorPickerView.OnColorListener {

    private FragmentCalendarStyleBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarStyleBinding.inflate(inflater, container, false);
        initColorPicker();
        return binding.getRoot();
    }

    private void initColorPicker() {
        ColorPickerView pickerView = binding.pickerView;
        pickerView.setListener(this);
        pickerView.setSelectedColor(getSelectedColor());
    }

    protected abstract int getSelectedColor();

    void saveColor(int code) {
        saveToPrefs(code);
        UpdatesHelper.getInstance(getContext()).updateCalendarWidget();
    }

    protected abstract void saveToPrefs(int code);

    @Override
    public void onColorSelect(int code) {
        saveColor(code);
    }
}
