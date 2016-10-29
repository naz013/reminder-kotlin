package com.elementary.tasks.navigation.settings.voice;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.FragmentTimeOfDayLayoutBinding;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

public class TimeOfDayFragment extends BaseSettingsFragment implements View.OnClickListener {

    private RoboTextView nightTime, eveningTime, dayTime, morningTime;
    private int morningHour, morningMinute;
    private int dayHour, dayMinute;
    private int eveningHour, eveningMinute;
    private int nightHour, nightMinute;
    private boolean is24;
    private SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentTimeOfDayLayoutBinding binding = FragmentTimeOfDayLayoutBinding.inflate(inflater, container, false);
        nightTime = binding.nightTime;
        nightTime.setOnClickListener(this);
        eveningTime = binding.eveningTime;
        eveningTime.setOnClickListener(this);
        dayTime = binding.dayTime;
        dayTime.setOnClickListener(this);
        morningTime = binding.morningTime;
        morningTime.setOnClickListener(this);

        is24 = Prefs.getInstance(mContext).is24HourFormatEnabled();

        initMorningTime();
        initNoonTime();
        initEveningTime();
        initNightTime();
        return binding.getRoot();
    }

    private void initNoonTime() {
        String noonTime = Prefs.getInstance(mContext).getNoonTime();
        Date date = null;
        try {
            date = format.parse(noonTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        if (date != null) calendar.setTime(date);
        dayHour = calendar.get(Calendar.HOUR_OF_DAY);
        dayMinute = calendar.get(Calendar.MINUTE);
        dayTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
    }

    private void initEveningTime() {
        String evening = Prefs.getInstance(mContext).getEveningTime();
        Date date = null;
        try {
            date = format.parse(evening);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        if (date != null) calendar.setTime(date);
        eveningHour = calendar.get(Calendar.HOUR_OF_DAY);
        eveningMinute = calendar.get(Calendar.MINUTE);
        eveningTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
    }

    private void initNightTime() {
        String night = Prefs.getInstance(mContext).getNightTime();
        Date date = null;
        try {
            date = format.parse(night);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (date != null) calendar.setTime(date);
        nightHour = calendar.get(Calendar.HOUR_OF_DAY);
        nightMinute = calendar.get(Calendar.MINUTE);
        nightTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
    }

    private void initMorningTime() {
        String morning = Prefs.getInstance(mContext).getMorningTime();
        Date date = null;
        try {
            date = format.parse(morning);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (date != null) calendar.setTime(date);
        morningHour = calendar.get(Calendar.HOUR_OF_DAY);
        morningMinute = calendar.get(Calendar.MINUTE);
        morningTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.time));
            mCallback.onFragmentSelect(this);
        }
    }

    private void morningDialog() {
        new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {
            morningHour = hourOfDay;
            morningMinute = minute;
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            String time = format.format(calendar.getTime());
            Prefs.getInstance(mContext).setMorningTime(time);
            morningTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
        }, morningHour, morningMinute, is24).show();
    }

    private void dayDialog() {
        new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {
            dayHour = hourOfDay;
            dayMinute = minute;
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            String time = format.format(calendar.getTime());
            Prefs.getInstance(mContext).setNoonTime(time);
            dayTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
        }, dayHour, dayMinute, is24).show();
    }

    private void nightDialog() {
        new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {
            nightHour = hourOfDay;
            nightMinute = minute;
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            String time = format.format(calendar.getTime());
            Prefs.getInstance(mContext).setNightTime(time);
            nightTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
        }, nightHour, nightMinute, is24).show();
    }

    private void eveningDialog() {
        new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {
            eveningHour = hourOfDay;
            eveningMinute = minute;
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            String time = format.format(calendar.getTime());
            Prefs.getInstance(mContext).setEveningTime(time);
            eveningTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
        }, eveningHour, eveningMinute,is24).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.morningTime:
                morningDialog();
                break;
            case R.id.dayTime:
                dayDialog();
                break;
            case R.id.eveningTime:
                eveningDialog();
                break;
            case R.id.nightTime:
                nightDialog();
                break;
        }
    }
}
