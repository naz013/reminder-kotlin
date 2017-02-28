package com.elementary.tasks.core.app_widgets.calendar;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.elementary.tasks.R;
import com.elementary.tasks.core.calendar.FlextHelper;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.FragmentCalendarWidgetPreviewBinding;
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import hirondelle.date4j.DateTime;

/**
 * Copyright 2015 Nazar Suhovich
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

public class CalendarThemeFragment extends BaseNavigationFragment {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_DATA = "arg_data";
    private int pageNumber;
    private List<CalendarTheme> list;

    private ThemeUtil themeUtil;

    public static CalendarThemeFragment newInstance(int page, List<CalendarTheme> list) {
        CalendarThemeFragment pageFragment = new CalendarThemeFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putParcelableArrayList(ARGUMENT_DATA, new ArrayList<>(list));
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intent = getArguments();
        pageNumber = intent.getInt(ARGUMENT_PAGE_NUMBER);
        list = intent.getParcelableArrayList(ARGUMENT_DATA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCalendarWidgetPreviewBinding binding = FragmentCalendarWidgetPreviewBinding.inflate(inflater, container, false);
        themeUtil = ThemeUtil.getInstance(getContext());
        CalendarTheme calendarTheme = list.get(pageNumber);
        int windowColor = calendarTheme.getWindowColor();
        binding.previewView.widgetBg.setBackgroundResource(windowColor);
        int windowTextColor = calendarTheme.getWindowTextColor();
        binding.themeTitle.setTextColor(windowTextColor);
        binding.note.setTextColor(windowTextColor);

        int itemTextColor = calendarTheme.getItemTextColor();
        int widgetBgColor = calendarTheme.getWidgetBgColor();
        int headerColor = calendarTheme.getHeaderColor();
        int borderColor = calendarTheme.getBorderColor();
        int titleColor = calendarTheme.getTitleColor();
        int rowColor = calendarTheme.getRowColor();

        int leftArrow = calendarTheme.getLeftArrow();
        int rightArrow = calendarTheme.getRightArrow();
        int iconPlus = calendarTheme.getIconPlus();
        int iconVoice = calendarTheme.getIconVoice();
        int iconSettings = calendarTheme.getIconSettings();

        int currentMark = calendarTheme.getCurrentMark();
        int birthdayMark = calendarTheme.getBirthdayMark();
        int reminderMark = calendarTheme.getReminderMark();

        binding.previewView.weekdayGrid.setBackgroundResource(widgetBgColor);
        binding.previewView.header.setBackgroundResource(headerColor);
        binding.previewView.currentDate.setTextColor(titleColor);
        binding.previewView.monthGrid.setBackgroundResource(borderColor);

        binding.previewView.plusButton.setImageResource(iconPlus);
        binding.previewView.nextMonth.setImageResource(rightArrow);
        binding.previewView.prevMonth.setImageResource(leftArrow);
        binding.previewView.voiceButton.setImageResource(iconVoice);
        binding.previewView.settingsButton.setImageResource(iconSettings);

        StringBuilder monthYearStringBuilder = new StringBuilder(50);
        Formatter monthYearFormatter = new Formatter(monthYearStringBuilder, Locale.getDefault());
        int monthYearFlag = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;
        Calendar cal = new GregorianCalendar();
        String monthTitle = DateUtils.formatDateRange(getActivity(),
                monthYearFormatter, cal.getTimeInMillis(), cal.getTimeInMillis(), monthYearFlag).toString();
        binding.previewView.currentDate.setText(monthTitle.toUpperCase());

        binding.themeTitle.setText(calendarTheme.getTitle());

        binding.previewView.weekdayGrid.setAdapter(new WeekdayAdapter(getActivity(), itemTextColor));
        binding.previewView.monthGrid.setAdapter(new MonthGridAdapter(getActivity(), new int[]{itemTextColor, rowColor,
                currentMark, birthdayMark, reminderMark}));
        return binding.getRoot();
    }

    private class WeekdayAdapter extends BaseAdapter{

        private List<String> weekdays;
        private int SUNDAY = 1;
        private int startDayOfWeek = SUNDAY;
        private Context context;
        private LayoutInflater inflater;
        private int textColor;

        WeekdayAdapter(Context context, int textColor){
            this.context = context;
            this.textColor = textColor;
            inflater = LayoutInflater.from(context);
            weekdays = new ArrayList<>();
            weekdays.clear();
            SimpleDateFormat fmt = new SimpleDateFormat("EEE", Locale.getDefault());

            // 17 Feb 2013 is Sunday
            DateTime sunday = new DateTime(2013, 2, 17, 0, 0, 0, 0);
            DateTime nextDay = sunday.plusDays(startDayOfWeek - SUNDAY);
            if (getPrefs().getStartDay() == 1){
                nextDay = nextDay.plusDays(1);
            }
            for (int i = 0; i < 7; i++) {
                Date date = FlextHelper.convertDateTimeToDate(nextDay);
                weekdays.add(fmt.format(date).toUpperCase());
                nextDay = nextDay.plusDays(1);
            }
        }

        @Override
        public int getCount() {
            return weekdays.size();
        }

        @Override
        public Object getItem(int position) {
            return weekdays.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.weekday_grid, null);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.textView1);
            textView.setText(weekdays.get(position));
            textView.setTextColor(textColor);
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    private class MonthGridAdapter extends BaseAdapter{

        List<DateTime> datetimeList;
        int SUNDAY = 1;
        int startDayOfWeek = SUNDAY;
        int prefsMonth;
        Context context;
        LayoutInflater inflater;
        int textColor;
        int widgetBgColor;
        int cMark;
        int bMark;
        int rMark;

        MonthGridAdapter(Context context, int[] resources){
            this.context = context;
            this.textColor = resources[0];
            this.widgetBgColor = resources[1];
            this.cMark = resources[2];
            this.bMark = resources[3];
            this.rMark = resources[4];
            inflater = LayoutInflater.from(context);
            datetimeList = new ArrayList<>();
            datetimeList.clear();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int year = calendar.get(Calendar.YEAR);

            DateTime firstDateOfMonth = new DateTime(year, prefsMonth + 1, 1, 0, 0, 0, 0);
            DateTime lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.getNumDaysInMonth() - 1);

            int weekdayOfFirstDate = firstDateOfMonth.getWeekDay();
            if (weekdayOfFirstDate < startDayOfWeek) {
                weekdayOfFirstDate += 7;
            }
            while (weekdayOfFirstDate > 0) {
                int temp = startDayOfWeek;
                if (Prefs.getInstance(context).getStartDay() == 1){
                    temp = startDayOfWeek + 1;
                }
                DateTime dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate - temp);
                if (!dateTime.lt(firstDateOfMonth)) {
                    break;
                }
                datetimeList.add(dateTime);
                weekdayOfFirstDate--;
            }
            for (int i = 0; i < lastDateOfMonth.getDay(); i++) {
                datetimeList.add(firstDateOfMonth.plusDays(i));
            }
            int endDayOfWeek = startDayOfWeek - 1;
            if (endDayOfWeek == 0) {
                endDayOfWeek = 7;
            }
            if (lastDateOfMonth.getWeekDay() != endDayOfWeek) {
                int i = 1;
                while (true) {
                    DateTime nextDay = lastDateOfMonth.plusDays(i);
                    datetimeList.add(nextDay);
                    i++;
                    if (nextDay.getWeekDay() == endDayOfWeek) {
                        break;
                    }
                }
            }
            int size = datetimeList.size();
            int numOfDays = 42 - size;
            DateTime lastDateTime = datetimeList.get(size - 1);
            for (int i = 1; i <= numOfDays; i++) {
                DateTime nextDateTime = lastDateTime.plusDays(i);
                datetimeList.add(nextDateTime);
            }
        }

        @Override
        public int getCount() {
            return datetimeList.size();
        }

        @Override
        public Object getItem(int position) {
            return datetimeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.month_view_grid, null);
            }
            int selDay = datetimeList.get(position).getDay();
            FrameLayout background = (FrameLayout) convertView.findViewById(R.id.background);
            TextView textView = (TextView) convertView.findViewById(R.id.textView);
            TextView currentMark = (TextView) convertView.findViewById(R.id.currentMark);
            TextView reminderMark = (TextView) convertView.findViewById(R.id.reminderMark);
            TextView birthdayMark = (TextView) convertView.findViewById(R.id.birthdayMark);

            textView.setText(String.valueOf(selDay));
            textView.setTextColor(textColor);
            background.setBackgroundResource(widgetBgColor);

            currentMark.setBackgroundColor(Color.TRANSPARENT);
            reminderMark.setBackgroundColor(Color.TRANSPARENT);
            birthdayMark.setBackgroundColor(Color.TRANSPARENT);
            if (selDay == 15){
                if (rMark != 0){
                    reminderMark.setBackgroundResource(rMark);
                } else {
                    reminderMark.setBackgroundColor(context.getResources()
                            .getColor(themeUtil.colorReminderCalendar()));
                }
            }
            if (selDay == 11){
                if (bMark != 0){
                    birthdayMark.setBackgroundResource(bMark);
                } else {
                    birthdayMark.setBackgroundColor(context.getResources()
                            .getColor(themeUtil.colorBirthdayCalendar()));
                }
            }
            if (11 == selDay){
                if (cMark != 0){
                    currentMark.setBackgroundResource(cMark);
                } else {
                    currentMark.setBackgroundColor(context.getResources()
                            .getColor(themeUtil.colorCurrentCalendar()));
                }
            }
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
