package com.elementary.tasks.navigation.fragments;

import android.app.AlarmManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.CalendarPagerAdapter;
import com.elementary.tasks.birthdays.EventsPagerItem;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.FragmentDayViewBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.viewpager.widget.ViewPager;

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
public class DayViewFragment extends BaseCalendarFragment {

    private static final String DATE_KEY = "date";
    private static final String POS_KEY = "position";

    private FragmentDayViewBinding binding;
    private ArrayList<EventsPagerItem> pagerData = new ArrayList<>();

    public static DayViewFragment newInstance(long date, int position) {
        DayViewFragment pageFragment = new DayViewFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(DATE_KEY, date);
        arguments.putInt(POS_KEY, position);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intent = getArguments();
        if (intent != null) {
            dateMills = intent.getLong(DATE_KEY, 0);
        }
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
                if (getCallback() != null) {
                    getCallback().onVoiceAction();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDayViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void updateMenuTitles() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (dateMills != 0) {
            calendar.setTimeInMillis(dateMills);
        }
        String dayString = TimeUtil.getDate(calendar.getTimeInMillis());
        if (getCallback() != null) getCallback().onTitleChange(dayString);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.calendar));
            getCallback().onFragmentSelect(this);
            getCallback().onMenuSelect(R.id.nav_day_view);
            getCallback().setClick(view -> showActionDialog(false));
        }
        loadData();
    }

    private void loadData() {
        initProvider();
        Calendar calendar = Calendar.getInstance();
        if (dateMills != 0) {
            calendar.setTimeInMillis(dateMills);
            showEvents(calendar.getTime());
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
            showEvents(calendar.getTime());
        }
    }

    private void showEvents(Date date) {
        pagerData.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int targetDay = calendar.get(Calendar.DAY_OF_MONTH);
        int targetMonth = calendar.get(Calendar.MONTH);
        int targetYear = calendar.get(Calendar.YEAR);

        calendar.setTimeInMillis(System.currentTimeMillis());

        int position = 0;
        int targetPosition = -1;
        calendar.add(Calendar.DAY_OF_MONTH, -100);
        while (position < Configs.MAX_DAYS_COUNT + 100) {
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
            int mMonth = calendar.get(Calendar.MONTH);
            int mYear = calendar.get(Calendar.YEAR);
            if (mDay == targetDay && mMonth == targetMonth && mYear == targetYear) {
                targetPosition = position;
                pagerData.add(new EventsPagerItem(position, 1, mDay, mMonth, mYear));
            } else {
                pagerData.add(new EventsPagerItem(position, 0, mDay, mMonth, mYear));
            }
            position++;
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        }
        CalendarPagerAdapter pagerAdapter = new CalendarPagerAdapter(Module.isJellyMR2() ? getChildFragmentManager() : getFragmentManager(), pagerData);
        try {
            binding.pager.setAdapter(pagerAdapter);
        } catch (IllegalStateException ignored) {
        }
        binding.pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                EventsPagerItem item = pagerData.get(i);
                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(item.getYear(), item.getMonth(), item.getDay());
                dateMills = calendar1.getTimeInMillis();
                updateMenuTitles();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        binding.pager.setCurrentItem(targetPosition);
        updateMenuTitles();
    }
}
