package com.elementary.tasks.core.additional;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TimePicker;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboCheckBox;
import com.elementary.tasks.core.views.roboto.RoboEditText;
import com.elementary.tasks.core.views.roboto.RoboRadioButton;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ActivityFollowLayoutBinding;
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

public class FollowReminderActivity extends ThemedActivity implements CompoundButton.OnCheckedChangeListener {

    private ActivityFollowLayoutBinding binding;
    private RoboEditText mMessageField;
    private RoboTextView mCustomDateView;
    private RoboTextView mCustomTimeView;
    private RoboRadioButton mMessageRadio;
    private RoboRadioButton mCallRadio;
    private RoboRadioButton mTomorrowRadio;
    private RoboRadioButton mNextWorkingRadio;
    private RoboRadioButton mAfterRadio;
    private RoboRadioButton mCustomRadio;
    private Spinner mAfterSpinner;
    private RoboCheckBox mTasksCheck;
    private RoboCheckBox mCalendarCheck;

    private int mHour = 0;
    private int mCustomHour = 0;
    private int mMinute = 0;
    private int mCustomMinute = 0;
    private int mYear = 0;
    private int mCustomYear = 0;
    private int mMonth = 0;
    private int mCustomMonth = 0;
    private int mDay = 1;
    private int mCustomDay = 1;
    private long mTomorrowTime;
    private long mNextWorkTime;
    private long mCurrentTime;

    private boolean mIs24Hour = true;
    private boolean mCalendar = true;
    private boolean mStock = true;
    private boolean mTasks = true;
    private String mNumber;

    private Google mGoogleTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleTasks = Google.getInstance(this);
        Intent i = getIntent();
        long receivedDate = i.getLongExtra(Constants.SELECTED_TIME, 0);
        mNumber = i.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
        String name = Contacts.getNameFromNumber(mNumber, FollowReminderActivity.this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_follow_layout);
        initActionBar();
        Calendar c = Calendar.getInstance();
        if (receivedDate != 0) {
            c.setTimeInMillis(receivedDate);
        } else c.setTimeInMillis(System.currentTimeMillis());
        mCurrentTime = c.getTimeInMillis();

        mMessageField = binding.textField;
        mMessageField.setHint(getString(R.string.message));

        RoboTextView contactInfo = binding.contactInfo;
        if (name != null && !name.matches("")) {
            contactInfo.setText(SuperUtil.appendString(name, "\n", mNumber));
        } else {
            contactInfo.setText(mNumber);
        }
        initViews();
        initPrefs();
        initExportChecks();
        initSpinner();
        initCustomTime();
        initTomorrowTime();
        initNextBusinessTime();
    }

    private void initViews() {
        mCustomTimeView = binding.customTime;
        mCustomDateView = binding.customDate;

        mMessageRadio = binding.typeMessage;
        mCallRadio = binding.typeCall;
        mCallRadio.setChecked(true);

        mTomorrowRadio = binding.timeTomorrow;
        mTomorrowRadio.setOnCheckedChangeListener(this);
        mAfterRadio = binding.timeAfter;
        mAfterRadio.setOnCheckedChangeListener(this);
        mCustomRadio = binding.timeCustom;
        mCustomRadio.setOnCheckedChangeListener(this);
        mNextWorkingRadio = binding.timeNextWorking;
        mNextWorkingRadio.setOnCheckedChangeListener(this);
        mTomorrowRadio.setChecked(true);
    }

    private void initNextBusinessTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mCurrentTime);
        int currDay = c.get(Calendar.DAY_OF_WEEK);
        if (currDay == Calendar.FRIDAY){
            c.setTimeInMillis(mCurrentTime + (1000 * 60 * 60 * 24 * 3));
        } else if (currDay == Calendar.SATURDAY){
            c.setTimeInMillis(mCurrentTime + (1000 * 60 * 60 * 24 * 2));
        } else {
            c.setTimeInMillis(mCurrentTime + (1000 * 60 * 60 * 24));
        }
        mNextWorkTime = c.getTimeInMillis();
        RoboTextView nextWorkingTime = binding.nextWorkingTime;
        nextWorkingTime.setText(TimeUtil.getDateTime(c.getTime(), mIs24Hour));
    }

    private void initTomorrowTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mCurrentTime + (1000 * 60 * 60 * 24));
        mTomorrowTime = c.getTimeInMillis();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        RoboTextView tomorrowTime = binding.tomorrowTime;
        tomorrowTime.setText(TimeUtil.getDateTime(c.getTime(), mIs24Hour));
    }

    private void initSpinner() {
        mAfterSpinner = binding.afterTime;
        mAfterSpinner.setAdapter(getAdapter());
    }

    private void initCustomTime() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mCurrentTime);
        mCustomDateView.setText(TimeUtil.DATE_FORMAT.format(c.getTime()));
        mCustomTimeView.setText(TimeUtil.getTime(c.getTime(), mIs24Hour));
        mCustomHour = c.get(Calendar.HOUR_OF_DAY);
        mCustomMinute = c.get(Calendar.MINUTE);
        mCustomYear = c.get(Calendar.YEAR);
        mCustomMonth = c.get(Calendar.MONTH);
        mCustomDay = c.get(Calendar.DAY_OF_MONTH);
        mCustomDateView.setOnClickListener(v -> {
            mCustomRadio.setChecked(true);
            dateDialog();
        });
        mCustomTimeView.setOnClickListener(v -> {
            mCustomRadio.setChecked(true);
            timeDialog();
        });
    }

    private void initExportChecks() {
        mCalendarCheck = binding.exportCheck;
        mTasksCheck = binding.taskExport;
        if (mCalendar || mStock){
            mCalendarCheck.setVisibility(View.VISIBLE);
        }
        if (mTasks){
            mTasksCheck.setVisibility(View.VISIBLE);
        }
        if (!mCalendar && !mStock && !mTasks) {
            binding.card5.setVisibility(View.GONE);
        }
    }

    private void initPrefs() {
        mCalendar = getPrefs().isCalendarEnabled();
        mStock = getPrefs().isStockCalendarEnabled();
        mTasks = mGoogleTasks != null;
        mIs24Hour = getPrefs().is24HourFormatEnabled();
    }

    private void initActionBar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        if (toolbar != null) {
            toolbar.setTitle(R.string.create_task);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        }
    }

    private SpinnerAdapter getAdapter() {
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(String.format(getString(R.string.x_minutes), String.valueOf(5)));
        spinnerArray.add(String.format(getString(R.string.x_minutes), String.valueOf(10)));
        spinnerArray.add(String.format(getString(R.string.x_minutes), String.valueOf(15)));
        spinnerArray.add(String.format(getString(R.string.x_minutes), String.valueOf(30)));
        spinnerArray.add(String.format(getString(R.string.x_minutes), String.valueOf(45)));
        spinnerArray.add(String.format(getString(R.string.x_minutes), String.valueOf(60)));
        spinnerArray.add(String.format(getString(R.string.x_hours), String.valueOf(2)));
        spinnerArray.add(String.format(getString(R.string.x_hours), String.valueOf(3)));
        spinnerArray.add(String.format(getString(R.string.x_hours), String.valueOf(4)));
        spinnerArray.add(String.format(getString(R.string.x_hours), String.valueOf(5)));
        return new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerArray);
    }

    private int getAfterMins(int progress) {
        int mins = 0;
        if (progress == 0) mins = 5;
        else if (progress == 1) mins = 10;
        else if (progress == 2) mins = 15;
        else if (progress == 3) mins = 30;
        else if (progress == 4) mins = 45;
        else if (progress == 5) mins = 60;
        else if (progress == 6) mins = 120;
        else if (progress == 7) mins = 180;
        else if (progress == 8) mins = 240;
        else if (progress == 9) mins = 300;
        return mins;
    }

    protected void dateDialog() {
        TimeUtil.showDatePicker(this, myDateCallBack, mYear, mMonth, mDay);
    }

    DatePickerDialog.OnDateSetListener myDateCallBack = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mCustomYear = year;
            mCustomMonth = monthOfYear;
            mCustomDay = dayOfMonth;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            mCustomDateView.setText(TimeUtil.DATE_FORMAT.format(c.getTime()));
        }
    };

    protected void timeDialog() {
        TimeUtil.showTimePicker(this, myCallBack, mCustomHour, mCustomMinute);
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCustomHour = hourOfDay;
            mCustomMinute = minute;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            mCustomTimeView.setText(TimeUtil.getTime(c.getTime(), mIs24Hour));
        }
    };

    private void saveDateTask(){
        String text = mMessageField.getText().toString().trim();
        if (text.matches("") && mMessageRadio.isChecked()){
            mMessageField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        int type = getType();
        setUpTimes();
        String categoryId = RealmDb.getInstance().getDefaultGroup().getUuId();
        long due = ReminderUtils.getTime(mDay, mMonth, mYear, mHour, mMinute, 0);
        Reminder reminder = new Reminder();
        reminder.setGroupUuId(categoryId);
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(due));
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(due));
        reminder.setType(type);
        reminder.setSummary(text);
        reminder.setTarget(mNumber);
        if (mTasksCheck.getVisibility() == View.VISIBLE) {
            reminder.setExportToTasks(mTasksCheck.isChecked());
        }
        if (mCalendarCheck.getVisibility() == View.VISIBLE) {
            reminder.setExportToCalendar(mCalendarCheck.isChecked());
        }
        EventControl control = EventControlFactory.getController(this, reminder);
        control.start();
        removeFlags();
        finish();
    }

    private void setUpTimes() {
        if (mNextWorkingRadio.isChecked()){
            setUpNextBusiness();
        } else if (mTomorrowRadio.isChecked()){
            setUpTomorrow();
        } else if (mCustomRadio.isChecked()){
            mDay = mCustomDay;
            mHour = mCustomHour;
            mMinute = mCustomMinute;
            mMonth = mCustomMonth;
            mYear = mCustomYear;
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(mCurrentTime + (1000 * 60 * getAfterMins(mAfterSpinner.getSelectedItemPosition())));
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
        }
    }

    private int getType() {
        if (mCallRadio.isChecked()) return Reminder.BY_DATE_CALL;
        else return Reminder.BY_DATE_SMS;
    }

    public void removeFlags(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.timeTomorrow:
                if (mTomorrowRadio.isChecked()) {
                    mNextWorkingRadio.setChecked(false);
                    mAfterRadio.setChecked(false);
                    mCustomRadio.setChecked(false);
                }
                setUpTomorrow();
                break;
            case R.id.timeNextWorking:
                if (mNextWorkingRadio.isChecked()) {
                    mTomorrowRadio.setChecked(false);
                    mAfterRadio.setChecked(false);
                    mCustomRadio.setChecked(false);
                }
                setUpNextBusiness();
                break;
            case R.id.timeAfter:
                if (mAfterRadio.isChecked()) {
                    mTomorrowRadio.setChecked(false);
                    mNextWorkingRadio.setChecked(false);
                    mCustomRadio.setChecked(false);
                }
                break;
            case R.id.timeCustom:
                if (mCustomRadio.isChecked()) {
                    mTomorrowRadio.setChecked(false);
                    mNextWorkingRadio.setChecked(false);
                    mAfterRadio.setChecked(false);
                }
                break;
        }
    }

    private void setUpNextBusiness() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mNextWorkTime);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
    }

    private void setUpTomorrow() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mTomorrowTime);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_follow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                saveDateTask();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
