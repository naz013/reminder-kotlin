package com.elementary.tasks.core.event_tree;

import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

/**
 * Copyright 2017 Nazar Suhovich
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

class Month implements TreeInterface, DayInterface {

    private int month;
    private Year year;
    private TreeMap<Integer, Day> nodes = new TreeMap<>();
    private int count = 0;

    Month(int month, Year year) {
        this.year = year;
        this.month = month;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year.getYear(), month, 1);
        int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= days; i++) {
            nodes.put(i, new Day(i, this));
        }
    }

    public int getMonth() {
        return month;
    }

    public Year getYear() {
        return year;
    }

    @Override
    public void addEvent(EventInterface eventInterface) {
        nodes.get(eventInterface.getDay()).addEvent(eventInterface);
        count++;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public List<EventInterface> getEvents(int y, int m, int d, int h, int min) {
        if (d == 0) return null;
        else return nodes.get(d).getEvents(y, m, d, h, min);
    }

    @Override
    public void remove(String uuId) {
        for (Day day : nodes.values()) {
            day.remove(uuId);
        }
    }

    @Override
    public void clearMinute(int year, int month, int day, int hour, int minute) {
        nodes.get(day).clearMinute(year, month, day, hour, minute);
    }

    @Override
    public void clearDay(int year, int month, int day) {
        nodes.put(day, new Day(day, this));
    }

    @Override
    public void clearHour(int year, int month, int day, int hour) {
        nodes.get(day).clearHour(year, month, day, hour);
    }
}
