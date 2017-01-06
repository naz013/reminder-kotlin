package com.elementary.tasks.core.event_tree;

import java.util.ArrayList;
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
    private int maxNodes;
    private Year year;
    private TreeMap<Integer, Day> nodes = new TreeMap<>();
    private int count = 0;

    Month(int month, Year year) {
        this.year = year;
        this.month = month;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year.getYear(), month, 1);
        maxNodes = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        return month;
    }

    public Year getYear() {
        return year;
    }

    @Override
    public void addNode(Object object) {
        EventInterface eventInterface = (EventInterface) object;
        int day = eventInterface.getDay();
        if (day < 1 || day > maxNodes) return;
        if (nodes.containsKey(day)) {
            nodes.get(day).addNode(object);
        } else {
            Day day1 = new Day(day, this);
            day1.addNode(object);
            nodes.put(day, day1);
        }
        count++;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public List<Object> getNodes(int... params) {
        if (params.length == 2) return getAll();
        int d = params[2];
        if (d == -1) return getAll();
        else if (d == 0) return null;
        if (nodes.containsKey(d)) {
            return nodes.get(d).getNodes(params);
        } else return null;
    }

    @Override
    public List<Object> getAll() {
        List<Object> list = new ArrayList<>();
        for (Day day : nodes.values()) {
            list.addAll(day.getAll());
        }
        return list;
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
