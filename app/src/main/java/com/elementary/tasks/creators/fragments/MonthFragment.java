package com.elementary.tasks.creators.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.ActionView;
import com.elementary.tasks.databinding.FragmentReminderMonthBinding;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.Calendar;
import java.util.Date;

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

public class MonthFragment extends RepeatableTypeFragment {

    private static final String TAG = "WeekFragment";
    private static final int CONTACTS = 114;

    protected int mHour = 0;
    protected int mMinute = 0;
    protected int mYear = 0;
    protected int mMonth = 0;
    protected int mDay = 1;

    private FragmentReminderMonthBinding binding;
    private ActionView.OnActionListener mActionListener = new ActionView.OnActionListener() {
        @Override
        public void onActionChange(boolean hasAction) {
            if (!hasAction) {
                getInterface().setEventHint(getString(R.string.remind_me));
                getInterface().setHasAutoExtra(false, null);
            }
        }

        @Override
        public void onTypeChange(boolean isMessageType) {
            if (isMessageType) {
                getInterface().setEventHint(getString(R.string.message));
                getInterface().setHasAutoExtra(true, getString(R.string.enable_sending_sms_automatically));
            } else {
                getInterface().setEventHint(getString(R.string.remind_me));
                getInterface().setHasAutoExtra(true, getString(R.string.enable_making_phone_calls_automatically));
            }
        }
    };
    private TimePickerDialog.OnTimeSetListener mTimeSelect = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            String formattedTime = TimeUtil.getTime(c.getTime(), Prefs.getInstance(getActivity()).is24HourFormatEnabled());
            binding.timeField.setText(formattedTime);
        }
    };
    private DatePickerDialog.OnDateSetListener mDateSelect = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
            if (dayOfMonth > 28) {
                getInterface().showSnackbar(getString(R.string.max_day_supported));
                return;
            }
            mDay = dayOfMonth;
            mMonth = monthOfYear;
            mYear = year;
            binding.monthDayField.setText(getZeroedInt(mDay));
        }
    };
    public View.OnClickListener dateClick = v -> TimeUtil.showDatePicker(getActivity(), mDateSelect, mYear, mMonth, mDay);
    public View.OnClickListener timeClick = v -> TimeUtil.showTimePicker(getActivity(), mTimeSelect, mHour, mMinute);

    public MonthFragment() {
    }

    @Override
    public boolean save() {
        if (getInterface() == null) return false;
        Reminder reminder = getInterface().getReminder();
        int type = Reminder.BY_MONTH;
        boolean isAction = binding.actionView.hasAction();
        if (TextUtils.isEmpty(getInterface().getSummary()) && !isAction) {
            getInterface().showSnackbar(getString(R.string.task_summary_is_empty));
            return false;
        }
        String number = null;
        if (isAction) {
            number = binding.actionView.getNumber();
            if (TextUtils.isEmpty(number)) {
                getInterface().showSnackbar(getString(R.string.you_dont_insert_number));
                return false;
            }
            if (binding.actionView.getType() == ActionView.TYPE_CALL) {
                type = Reminder.BY_MONTH_CALL;
            } else {
                type = Reminder.BY_MONTH_SMS;
            }
        }
        if (reminder == null) {
            reminder = new Reminder();
        }
        reminder.setWeekdays(null);
        reminder.setTarget(number);
        reminder.setType(type);
        reminder.setDayOfMonth(mDay);
        reminder.setRepeatInterval(0);
        reminder.setExportToCalendar(binding.exportToCalendar.isChecked());
        reminder.setExportToTasks(binding.exportToTasks.isChecked());
        reminder.setClear(getInterface());
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(getTime()));
        reminder.setRemindBefore(binding.beforeView.getBeforeValue());
        long startTime = TimeCount.getInstance(getContext()).getNextMonthDayTime(reminder);
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        LogUtil.d(TAG, "EVENT_TIME " + TimeUtil.getFullDateTime(startTime, true, true));
        if (!TimeCount.isCurrent(reminder.getEventTime())) {
            Toast.makeText(getContext(), R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
            return false;
        }
        EventControl control = EventControlFactory.getController(getContext(), reminder);
        return control.start();
    }

    private long getTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, mHour);
        calendar.set(Calendar.MINUTE, mMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
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
        binding = FragmentReminderMonthBinding.inflate(inflater, container, false);
        binding.monthDayField.setOnClickListener(dateClick);
        binding.timeField.setOnClickListener(timeClick);
        binding.timeField.setText(TimeUtil.getTime(updateTime(System.currentTimeMillis()),
                Prefs.getInstance(getActivity()).is24HourFormatEnabled()));
        binding.actionView.setListener(mActionListener);
        binding.actionView.setActivity(getActivity());
        binding.actionView.setContactClickListener(view -> selectContact());
        binding.lastCheck.setOnCheckedChangeListener((compoundButton, b) -> changeUi(b));
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        editReminder();
        showSelectedDay();
        return binding.getRoot();
    }

    private void showSelectedDay() {
        if (mDay == 0) {
            mDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        }
        if (mDay > 28) mDay = 28;
        binding.monthDayField.setText(getZeroedInt(mDay));
    }

    private void changeUi(boolean b) {
        if (b) {
            ViewUtils.collapse(binding.dayView);
            mDay = 0;
        } else {
            ViewUtils.expand(binding.dayView);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            if (mDay > 28) mDay = 1;
        }
    }

    protected Date updateTime(long millis) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        mHour = cal.get(Calendar.HOUR_OF_DAY);
        mMinute = cal.get(Calendar.MINUTE);
        return cal.getTime();
    }

    private void editReminder() {
        if (getInterface().getReminder() == null) return;
        Reminder reminder = getInterface().getReminder();
        binding.exportToCalendar.setChecked(reminder.isExportToCalendar());
        binding.exportToTasks.setChecked(reminder.isExportToTasks());
        binding.timeField.setText(TimeUtil.getTime(updateTime(TimeUtil.getDateTimeFromGmt(reminder.getEventTime())),
                Prefs.getInstance(getActivity()).is24HourFormatEnabled()));
        if (reminder.getDayOfMonth() == 0) {
            binding.lastCheck.setChecked(true);
        } else {
            mDay = reminder.getDayOfMonth();
            binding.dayCheck.setChecked(true);
        }
        binding.beforeView.setBefore(reminder.getRemindBefore());
        if (reminder.getTarget() != null) {
            binding.actionView.setAction(true);
            binding.actionView.setNumber(reminder.getTarget());
            if (Reminder.isKind(reminder.getType(), Reminder.Kind.CALL)) {
                binding.actionView.setType(ActionView.TYPE_CALL);
            } else if (Reminder.isKind(reminder.getType(), Reminder.Kind.SMS)) {
                binding.actionView.setType(ActionView.TYPE_MESSAGE);
            }
        }
    }

    private void selectContact() {
        if (Permissions.checkPermission(getActivity(), Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(getActivity(), Constants.REQUEST_CODE_CONTACTS);
        } else {
            Permissions.requestPermission(getActivity(), CONTACTS, Permissions.READ_CONTACTS, Permissions.READ_CALLS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS && resultCode == Activity.RESULT_OK) {
            String number = data.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
            binding.actionView.setNumber(number);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectContact();
                }
                break;
            case DateFragment.CONTACTS_ACTION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.actionView.setAction(true);
                }
                break;
        }
    }
}
