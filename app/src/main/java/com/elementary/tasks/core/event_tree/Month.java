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
    public void buildTree(Param params, int position) {
        int day = params.getDay();
        if (nodes.containsKey(day)) {
            Day d = nodes.get(day);
            d.buildTree(params, position);
        } else {
            Day d = new Day(day, this);
            d.buildTree(params, position);
            nodes.put(day, d);
        }
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public List<Integer> getAll() {
        List<Integer> list = new ArrayList<>();
        for (Day day : nodes.values()) {
            list.addAll(day.getAll());
        }
        return list;
    }

    @Override
    public List<Integer> getNodes(Param params) {
        int day = params.getDay();
        if (nodes.containsKey(day)) {
            return nodes.get(day).getAll();
        }
        return null;
    }

    @Override
    public void remove(Param params, int position) {
        int day = params.getDay();
        if (nodes.containsKey(day)) {
            Day d = nodes.get(day);
            d.remove(params, position);
            if (d.isEmpty()) {
                nodes.remove(d.getDay());
            }
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

    @Override
    public void print() {
        System.out.println("MONTH -> " + month + ", CONTENT: " + nodes.keySet());
        for (Day day : nodes.values()) {
            day.print();
        }
    }
}
