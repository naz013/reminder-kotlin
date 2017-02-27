package com.elementary.tasks.navigation.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;

import com.elementary.tasks.birthdays.AddBirthdayActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.databinding.DialogActionPickerBinding;
import com.elementary.tasks.reminder.AddReminderActivity;

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

    protected static final int REMINDER_CODE = 1110;
    protected static final int BD_CODE = 1111;

    protected long dateMills;
    private AlertDialog mDialog;

    protected void showActionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogActionPickerBinding binding = DialogActionPickerBinding.inflate(LayoutInflater.from(getContext()));
        binding.addBirth.setOnClickListener(view -> {
            mDialog.dismiss();
            addBirthday();
        });
        binding.addEvent.setOnClickListener(view -> {
            mDialog.dismiss();
            addReminder();
        });
        builder.setView(binding.getRoot());
        mDialog = builder.create();
        mDialog.show();
    }

    private void addReminder() {
        getActivity().startActivityForResult(new Intent(getContext(), AddReminderActivity.class).putExtra(Constants.INTENT_DATE, dateMills), REMINDER_CODE);
    }

    private void addBirthday() {
        getActivity().startActivityForResult(new Intent(getContext(), AddBirthdayActivity.class).putExtra(Constants.INTENT_DATE, dateMills), BD_CODE);
    }
}
