package com.elementary.tasks.navigation.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.EventsDataProvider;
import com.elementary.tasks.core.calendar.FlextCalendarFragment;
import com.elementary.tasks.core.calendar.FlextListener;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.navigation.settings.images.MonthImage;

import java.util.Arrays;
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

public class CalendarFragment extends BaseCalendarFragment {

    private static final String TAG = "CalendarFragment";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.day_view_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_voice:
                if (mCallback != null){
                    mCallback.onVoiceAction();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.calendar));
            mCallback.onFragmentSelect(this);
            mCallback.setClick(view -> {
                dateMills = System.currentTimeMillis();
                showActionDialog();
            });
        }
        showCalendar();
    }

    private void showCalendar() {
        ThemeUtil themeUtil = ThemeUtil.getInstance(mContext);
        FlextCalendarFragment calendarView = new FlextCalendarFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        args.putInt(FlextCalendarFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(FlextCalendarFragment.YEAR, cal.get(Calendar.YEAR));
        if (mPrefs.getStartDay() == 0) {
            args.putInt(FlextCalendarFragment.START_DAY_OF_WEEK, FlextCalendarFragment.SUNDAY);
        } else {
            args.putInt(FlextCalendarFragment.START_DAY_OF_WEEK, FlextCalendarFragment.MONDAY);
        }
        args.putBoolean(FlextCalendarFragment.DARK_THEME, themeUtil.isDark());
        args.putBoolean(FlextCalendarFragment.ENABLE_IMAGES, mPrefs.isCalendarImagesEnabled());
        MonthImage monthImage = mPrefs.getCalendarImages();
        LogUtil.d(TAG, "showCalendar: " + Arrays.toString(monthImage.getPhotos()));
        args.putLongArray(FlextCalendarFragment.MONTH_IMAGES, monthImage.getPhotos());
        calendarView.setArguments(args);
        final FlextListener listener = new FlextListener() {
            @Override
            public void onClickDate(Date date) {
                LogUtil.d(TAG, "onClick: " + date);
                saveTime(date);
                replaceFragment(DayViewFragment.newInstance(dateMills, 0), "");
            }

            @Override
            public void onLongClickDate(Date date) {
                LogUtil.d(TAG, "onLongClickDate: " + date);
                saveTime(date);
                showActionDialog();
            }

            @Override
            public void onMonthChanged(int month, int year) {

            }

            @Override
            public void onViewCreated() {
            }

            @Override
            public void onMonthSelected(int month) {

            }
        };
        calendarView.setListener(listener);
        calendarView.refreshView();
        replaceFragment(calendarView, getString(R.string.calendar));
        boolean isReminder = mPrefs.isRemindersInCalendarEnabled();
        boolean isFeature = mPrefs.isFutureEventEnabled();
        calendarView.setEvents(new EventsDataProvider(mContext, isReminder, isFeature).getEvents());
        getActivity().invalidateOptionsMenu();
    }

    private void saveTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        dateMills = calendar.getTimeInMillis();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REMINDER_CODE || requestCode == BD_CODE && resultCode == Activity.RESULT_OK) {
            showCalendar();
        }
    }
}
