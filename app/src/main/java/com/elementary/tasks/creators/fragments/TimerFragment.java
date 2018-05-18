package com.elementary.tasks.creators.fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.DialogExclusionPickerBinding;
import com.elementary.tasks.databinding.FragmentTimerBinding;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

public class TimerFragment extends RepeatableTypeFragment {

    private static final String TAG = "TimerFragment";

    private FragmentTimerBinding binding;

    private List<Integer> mHours = new ArrayList<>();
    private String mFrom, mTo;
    private int fromHour, fromMinute;
    private int toHour, toMinute;
    private ArrayList<ToggleButton> buttons;

    public TimerFragment() {

    }

    @Override
    public Reminder prepare() {
        if (getInterface() == null) return null;
        long after = binding.timerPickerView.getTimerValue();
        if (after == 0) {
            Toast.makeText(getContext(), getString(R.string.you_dont_insert_timer_time), Toast.LENGTH_SHORT).show();
            return null;
        }
        Reminder reminder = getInterface().getReminder();
        if (reminder == null) {
            reminder = new Reminder();
        }

        int type = Reminder.BY_TIME;
        reminder.setType(type);
        reminder.setAfter(after);
        long repeat = binding.repeatView.getRepeat();
        reminder.setRepeatInterval(repeat);
        reminder.setExportToCalendar(binding.exportToCalendar.isChecked());
        reminder.setExportToTasks(binding.exportToTasks.isChecked());
        reminder.setFrom(mFrom);
        reminder.setTo(mTo);
        reminder.setHours(mHours);
        reminder.setClear(getInterface());
        long startTime = TimeCount.getInstance(getContext()).generateNextTimer(reminder, true);
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true));
        if (!TimeCount.isCurrent(reminder.getEventTime())) {
            Toast.makeText(getContext(), R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
            return null;
        }
        return reminder;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_date_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_limit:
                changeLimit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTimerBinding.inflate(inflater, container, false);
        binding.timerPickerView.setListener(binding.repeatView.getTimerListener());
        getInterface().setExclusionAction(view -> openExclusionDialog());
        if (getInterface().isExportToCalendar()) {
            binding.exportToCalendar.setVisibility(View.VISIBLE);
        } else {
            binding.exportToCalendar.setVisibility(View.GONE);
        }
        if (getInterface().isExportToTasks()) {
            binding.exportToTasks.setVisibility(View.VISIBLE);
        } else {
            binding.exportToTasks.setVisibility(View.GONE);
        }
        editReminder();
        return binding.getRoot();
    }

    private void editReminder() {
        if (getInterface().getReminder() == null) return;
        Reminder reminder = getInterface().getReminder();
        binding.exportToCalendar.setChecked(reminder.isExportToCalendar());
        binding.exportToTasks.setChecked(reminder.isExportToTasks());
        binding.repeatView.setRepeat(reminder.getRepeatInterval());
        binding.timerPickerView.setTimerValue(reminder.getAfter());
        this.mFrom = reminder.getFrom();
        this.mTo = reminder.getTo();
        this.mHours = reminder.getHours();
    }

    private void openExclusionDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(R.string.exclusion);
        DialogExclusionPickerBinding b = getCustomizationView();
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialog, which) -> saveExclusion(b));
        builder.setNegativeButton(R.string.remove_exclusion, (dialogInterface, i) -> clearExclusion());
        builder.create().show();
    }

    private void clearExclusion() {
        mHours.clear();
        mFrom = null;
        mTo = null;
    }

    private void saveExclusion(DialogExclusionPickerBinding b) {
        clearExclusion();
        if (b.selectHours.isChecked()) {
            mHours = getSelectedList();
            if (mHours.size() == 0) {
                Toast.makeText(getContext(), getString(R.string.you_dont_select_any_hours), Toast.LENGTH_SHORT).show();
            }
        } else if (b.selectInterval.isChecked()) {
            mFrom = getHour(fromHour, fromMinute);
            mTo = getHour(toHour, toMinute);
        }
    }

    @SuppressLint("ResourceType")
    private List<Integer> getSelectedList() {
        List<Integer> ids = new ArrayList<>();
        for (ToggleButton button : buttons) {
            if (button.isChecked()) ids.add(button.getId() - 100);
        }
        return ids;
    }

    private String getHour(int hour, int minute) {
        return hour + ":" + minute;
    }

    private DialogExclusionPickerBinding getCustomizationView() {
        DialogExclusionPickerBinding binding = DialogExclusionPickerBinding.inflate(LayoutInflater.from(getContext()));
        binding.selectInterval.setChecked(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        fromHour = calendar.get(Calendar.HOUR_OF_DAY);
        fromMinute = calendar.get(Calendar.MINUTE);
        binding.from.setText(getString(R.string.from) + " " + TimeUtil.getTime(calendar.getTime(), true));
        calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_HOUR * 3);
        toHour = calendar.get(Calendar.HOUR_OF_DAY);
        toMinute = calendar.get(Calendar.MINUTE);
        binding.to.setText(getString(R.string.to) + " " + TimeUtil.getTime(calendar.getTime(), true));
        binding.from.setOnClickListener(v -> fromTime(binding.from));
        binding.to.setOnClickListener(v -> toTime(binding.to));
        initButtons(binding);
        if (mFrom != null && mTo != null) {
            calendar.setTime(TimeUtil.getDate(mFrom));
            fromHour = calendar.get(Calendar.HOUR_OF_DAY);
            fromMinute = calendar.get(Calendar.MINUTE);
            calendar.setTime(TimeUtil.getDate(mTo));
            toHour = calendar.get(Calendar.HOUR_OF_DAY);
            toMinute = calendar.get(Calendar.MINUTE);
            binding.selectInterval.setChecked(true);
        }
        if (mHours != null && mHours.size() > 0) {
            binding.selectHours.setChecked(true);
        }
        return binding;
    }

    private void initButtons(DialogExclusionPickerBinding b) {
        setId(b.zero, b.one, b.two, b.three, b.four, b.five, b.six, b.seven, b.eight, b.nine, b.ten,
                b.eleven, b.twelve, b.thirteen, b.fourteen, b.fifteen, b.sixteen, b.seventeen,
                b.eighteen, b.nineteen, b.twenty, b.twentyOne, b.twentyThree, b.twentyTwo);
    }

    private void setId(ToggleButton... buttons) {
        int i = 100;
        ThemeUtil cs = ThemeUtil.getInstance(getContext());
        this.buttons = new ArrayList<>();
        List<Integer> selected = new ArrayList<>(mHours);
        for (ToggleButton button : buttons) {
            button.setId(i);
            button.setBackgroundDrawable(cs.toggleDrawable());
            this.buttons.add(button);
            if (selected.contains(i - 100)) button.setChecked(true);
            i++;
        }
    }

    private void fromTime(RoboTextView textView) {
        TimeUtil.showTimePicker(getContext(), (view, hourOfDay, minute) -> {
            fromHour = hourOfDay;
            fromMinute = minute;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            textView.setText(getString(R.string.from) + " " + TimeUtil.getTime(calendar.getTime(), true));
        }, fromHour, fromMinute);
    }

    private void toTime(RoboTextView textView) {
        TimeUtil.showTimePicker(getContext(), (view, hourOfDay, minute) -> {
            toHour = hourOfDay;
            toMinute = minute;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            textView.setText(getString(R.string.to) + " " + TimeUtil.getTime(calendar.getTime(), true));
        }, toHour, toMinute);
    }
}
