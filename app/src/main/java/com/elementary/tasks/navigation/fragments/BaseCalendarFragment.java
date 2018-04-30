package com.elementary.tasks.navigation.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.AddBirthdayActivity;
import com.elementary.tasks.birthdays.CalendarEventsAdapter;
import com.elementary.tasks.birthdays.DayViewProvider;
import com.elementary.tasks.birthdays.EventsDataSingleton;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.DialogActionPickerBinding;
import com.elementary.tasks.reminder.AddReminderActivity;

import java.util.Calendar;

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

    protected void showActionDialog(boolean showEvents) {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        DialogActionPickerBinding binding = DialogActionPickerBinding.inflate(LayoutInflater.from(getContext()));
        binding.addBirth.setOnClickListener(view -> {
            mDialog.dismiss();
            addBirthday();
        });
        binding.addBirth.setOnLongClickListener(v -> {
            showMessage(getString(R.string.add_birthday));
            return true;
        });
        binding.addEvent.setOnClickListener(view -> {
            mDialog.dismiss();
            addReminder();
        });
        binding.addEvent.setOnLongClickListener(v -> {
            showMessage(getString(R.string.add_reminder_menu));
            return true;
        });
        if (showEvents && dateMills != 0) {
            binding.loadingView.setVisibility(View.VISIBLE);
            binding.eventsList.setLayoutManager(new LinearLayoutManager(getContext()));
            loadEvents(binding);
        } else {
            binding.loadingView.setVisibility(View.GONE);
        }
        if (dateMills != 0) {
            binding.dateLabel.setText(TimeUtil.getDate(dateMills));
        }
        builder.setView(binding.getRoot());
        mDialog = builder.create();
        mDialog.show();
    }

    private void showMessage(String string) {
        Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
    }

    private void loadEvents(DialogActionPickerBinding binding) {
        DayViewProvider provider = EventsDataSingleton.getInstance().getProvider();
        if (provider != null && provider.isReady()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dateMills);
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
            int mMonth = calendar.get(Calendar.MONTH);
            int mYear = calendar.get(Calendar.YEAR);
            provider.findMatches(mDay, mMonth, mYear, true, list -> {
                if (binding != null && getContext() != null) {
                    CalendarEventsAdapter mAdapter = new CalendarEventsAdapter(getContext(), list);
                    binding.eventsList.setAdapter(mAdapter);
                    binding.eventsList.setVisibility(View.VISIBLE);
                    binding.loadingView.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initProvider();
    }

    protected void initProvider() {
        String time = getPrefs().getBirthdayTime();
        boolean isFeature = getPrefs().isFutureEventEnabled();
        boolean isRemindersEnabled = getPrefs().isRemindersInCalendarEnabled();
        DayViewProvider provider = EventsDataSingleton.getInstance().getProvider();
        if (provider == null) {
            provider = new DayViewProvider(getContext());
            EventsDataSingleton.getInstance().setProvider(provider);
        }
        if (!provider.isInProgress()) {
            provider.setBirthdays(true);
            provider.setTime(TimeUtil.getBirthdayTime(time));
            provider.setReminders(isRemindersEnabled);
            provider.setFeature(isFeature);
            provider.fillArray();
        }
    }

    private void addReminder() {
        getActivity().startActivityForResult(new Intent(getContext(), AddReminderActivity.class)
                .putExtra(Constants.INTENT_DATE, dateMills), REMINDER_CODE);
    }

    private void addBirthday() {
        getActivity().startActivityForResult(new Intent(getContext(), AddBirthdayActivity.class)
                .putExtra(Constants.INTENT_DATE, dateMills), BD_CODE);
    }
}
