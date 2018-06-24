package com.elementary.tasks.core.calendar;

import android.os.Bundle;
import androidx.annotation.IntRange;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.views.MonthView;
import com.elementary.tasks.databinding.FragmentDateGridBinding;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
public class DateGridFragment extends Fragment {

    private MonthView monthView;

    private Map<DateTime, Events> eventsMap = new HashMap<>();
    @IntRange(from = 1, to = 12)
    private int month;
    private int year;

    private MonthView.OnDateClick onItemClickListener;
    private MonthView.OnDateLongClick onItemLongClickListener;

    public void setDateTime(DateTime dateTime) {
        this.month = dateTime.getMonth();
        this.year = dateTime.getYear();
        if (monthView != null) {
            monthView.setDate(year, month);
            monthView.invalidate();
        }
    }

    public void setDate(int month, int year) {
        this.month = month;
        this.year = year;
        if (monthView != null) {
            monthView.setDate(year, month);
            monthView.invalidate();
        }
    }

    public void setEventsMap(Map<DateTime, Events> eventsMap) {
        this.eventsMap = eventsMap;
    }

    public void setOnItemClickListener(MonthView.OnDateClick onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(MonthView.OnDateLongClick onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDateGridBinding v = FragmentDateGridBinding.inflate(inflater, container, false);
        monthView = v.monthView;
        if (year != 0) {
            monthView.setDate(year, month);
        }
        monthView.setEventsMap(eventsMap);
        if (onItemClickListener != null) {
            monthView.setDateClick(onItemClickListener);
        }
        if (onItemLongClickListener != null) {
            monthView.setDateLongClick(onItemLongClickListener);
        }
        monthView.invalidate();
        return v.getRoot();
    }
}
