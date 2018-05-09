package com.elementary.tasks.core.app_widgets.events;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.databinding.FragmentEventsWidgetPreviewBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import androidx.fragment.app.Fragment;

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

public class EventsThemeFragment extends Fragment {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_DATA = "arg_data";
    private int mPageNumber;
    private List<EventsTheme> mList;

    public static EventsThemeFragment newInstance(int page, List<EventsTheme> list) {
        EventsThemeFragment pageFragment = new EventsThemeFragment();
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
        mPageNumber = intent.getInt(ARGUMENT_PAGE_NUMBER);
        mList = intent.getParcelableArrayList(ARGUMENT_DATA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentEventsWidgetPreviewBinding binding = FragmentEventsWidgetPreviewBinding.inflate(inflater, container, false);

        EventsTheme eventsTheme = mList.get(mPageNumber);

        int windowColor = eventsTheme.getWindowColor();
        binding.background.setBackgroundResource(windowColor);
        int windowTextColor = eventsTheme.getWindowTextColor();
        binding.themeTitle.setTextColor(windowTextColor);
        binding.themeTip.setTextColor(windowTextColor);

        int headerColor = eventsTheme.getHeaderColor();
        int backgroundColor = eventsTheme.getBackgroundColor();
        int titleColor = eventsTheme.getTitleColor();
        int itemTextColor = eventsTheme.getItemTextColor();
        int itemBackground = eventsTheme.getItemBackground();

        int settingsIcon = eventsTheme.getSettingsIcon();
        int plusIcon = eventsTheme.getPlusIcon();
        int voiceIcon = eventsTheme.getVoiceIcon();

        binding.widgetDate.setTextColor(titleColor);
        binding.taskText.setTextColor(itemTextColor);
        binding.taskNumber.setTextColor(itemTextColor);
        binding.taskDate.setTextColor(itemTextColor);
        binding.taskTime.setTextColor(itemTextColor);

        binding.headerBg.setBackgroundResource(headerColor);
        binding.widgetBg.setBackgroundResource(backgroundColor);
        binding.listItemCard.setBackgroundResource(itemBackground);

        binding.plusButton.setImageResource(plusIcon);
        binding.optionsButton.setImageResource(settingsIcon);
        binding.voiceButton.setImageResource(voiceIcon);

        binding.themeTitle.setText(eventsTheme.getTitle());

        StringBuilder monthYearStringBuilder = new StringBuilder(50);
        Formatter monthYearFormatter = new Formatter(monthYearStringBuilder, Locale.getDefault());
        int monthYearFlag = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;
        Calendar cal = new GregorianCalendar();
        String monthTitle = DateUtils.formatDateRange(getActivity(),
                monthYearFormatter, cal.getTimeInMillis(), cal.getTimeInMillis(), monthYearFlag).toString();
        binding.widgetDate.setText(monthTitle.toUpperCase());
        return binding.getRoot();
    }
}
