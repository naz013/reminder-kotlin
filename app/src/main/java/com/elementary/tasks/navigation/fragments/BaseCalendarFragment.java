package com.elementary.tasks.navigation.fragments;

import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.DialogActionPickerBinding;

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

public abstract class BaseCalendarFragment extends BaseNavigationFragment {

    protected long dateMills;
    private AlertDialog mDialog;

    protected void showActionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        DialogActionPickerBinding binding = DialogActionPickerBinding.inflate(LayoutInflater.from(mContext));
        binding.addBirth.setOnClickListener(view -> {
            mDialog.dismiss();
            addBirthday();
        });
        binding.addEvent.setOnClickListener(view -> {
            mDialog.dismiss();
            addReminder();
        });
        if (ThemeUtil.getInstance(mContext).isDark()) {
            binding.addEvent.setImageResource(R.drawable.ic_alarm_white);
            binding.addBirth.setImageResource(R.drawable.ic_cake_white_24dp);
        } else {
            binding.addEvent.setImageResource(R.drawable.ic_alarm);
            binding.addBirth.setImageResource(R.drawable.ic_cake_black_24dp);
        }
        mDialog.setView(binding.getRoot());
        mDialog = builder.create();
        mDialog.show();
    }

    private void addReminder() {

    }

    private void addBirthday() {

    }
}
