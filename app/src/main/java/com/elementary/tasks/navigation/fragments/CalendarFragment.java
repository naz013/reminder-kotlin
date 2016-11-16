package com.elementary.tasks.navigation.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.views.MonthView;
import com.elementary.tasks.databinding.FragmentCalendarBinding;

import hirondelle.date4j.DateTime;

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

public class CalendarFragment extends BaseNavigationFragment {

    private static final String TAG = "CalendarFragment";

    private FragmentCalendarBinding binding;
    private MonthView.OnDateClick mDateClick = new MonthView.OnDateClick() {
        @Override
        public void onClick(DateTime dateTime) {
            Log.d(TAG, "onClick: " + dateTime.getDay());
        }
    };
    private MonthView.OnDateLongClick mDateLongClick = new MonthView.OnDateLongClick() {
        @Override
        public void onLongClick(DateTime dateTime) {
            Log.d(TAG, "onLongClick: " + dateTime.getDay());
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        binding.calendarView.setDateClick(mDateClick);
        binding.calendarView.setDateLongClick(mDateLongClick);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.calendar));
            mCallback.onFragmentSelect(this);
            mCallback.setClick(null);
        }
    }
}
