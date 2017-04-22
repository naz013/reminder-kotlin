package com.elementary.tasks.core.event_tree;

import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class EventRoot implements YearInterface, SearchInterface {

    private Map<Integer, Year> nodes = new TreeMap<>();
    private List<Node> keys = new ArrayList<>();
    private Map<String, Integer> map = new HashMap<>();
    private long birthdayTime;

    public EventRoot() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        if (!nodes.containsKey(year)) {
            Year y = new Year(year);
            nodes.put(year, y);
        }
    }

    public void setBirthdayTime(long birthdayTime) {
        this.birthdayTime = birthdayTime;
    }

    public void addReminder(Reminder reminder) {
        if (map.containsKey(reminder.getUuId())) {
            int position = map.get(reminder.getUuId());
            removeCurrent(position);
        }
        Param param = new Param(reminder.getKeys());
        int year = param.getYear();
        keys.add(new Node(reminder, reminder.getUuId(), param, reminder.getDuration()));
        int position = keys.size() - 1;
        map.put(reminder.getUuId(), position);
        if (nodes.containsKey(year)) {
            nodes.get(year).buildTree(param, position);
        } else {
            Year y = new Year(year);
            y.buildTree(param, position);
            nodes.put(year, y);
        }
    }

    private void removeCurrent(int position) {

    }

    public void addBirthday(BirthdayItem item, long duration) {

    }

    public int size() {
        return keys.size();
    }

    @Override
    public void clearMinute(int year, int month, int day, int hour, int minute) {
        nodes.get(year).clearMinute(year, month, day, hour, minute);
    }

    @Override
    public void clearYear(int year) {
        nodes.put(year, new Year(year));
    }

    @Override
    public void clearHour(int year, int month, int day, int hour) {
        nodes.get(year).clearHour(year, month, day, hour);
    }

    @Override
    public void clearDay(int year, int month, int day) {
        nodes.get(year).clearDay(year, month, day);
    }

    @Override
    public void clearMonth(int year, int month) {
        nodes.get(year).clearMonth(year, month);
    }

    public void print() {
        System.out.println("ROOT -> CONTENT: " + nodes.keySet());
        for (Year year : nodes.values()) {
            year.print();
        }
    }

    @Override
    public void find(long startMills, long endMills, long required, SearchCallback callback) {

    }
}
