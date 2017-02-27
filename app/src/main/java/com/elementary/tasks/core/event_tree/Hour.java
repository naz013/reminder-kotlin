package com.elementary.tasks.core.event_tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import hirondelle.date4j.DateTime;

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

class Hour implements TreeInterface, MinuteInterface, SearchInterface {

    private int hour;
    private Day day;
    private Map<Integer, Minute> nodes = new TreeMap<>();

    Hour(int hour, Day day) {
        this.day = day;
        this.hour = hour;
    }

    public Day getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    @Override
    public void buildTree(Param params, int position) {
        int minute = params.getMinute();
        if (nodes.containsKey(minute)) {
            Minute m = nodes.get(minute);
            m.buildTree(params, position);
        } else {
            Minute m = new Minute(minute, this);
            m.buildTree(params, position);
            nodes.put(minute, m);
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
        for (Minute minute : nodes.values()) {
            list.addAll(minute.getAll());
        }
        return list;
    }

    @Override
    public List<Integer> getNodes(Param params) {
        int minute = params.getMinute();
        if (nodes.containsKey(minute)) {
            return nodes.get(minute).getAll();
        }
        return null;
    }

    @Override
    public void remove(Param params, int position) {
        int minute = params.getMinute();
        if (nodes.containsKey(minute)) {
            Minute m = nodes.get(minute);
            m.remove(params, position);
            if (m.isEmpty()) {
                nodes.remove(m.getMinute());
            }
        }
    }


    @Override
    public void clearMinute(int year, int month, int day, int hour, int minute) {
        nodes.put(minute, new Minute(minute, this));
    }

    @Override
    public void print() {
        System.out.println("HOUR -> " + hour + ", CONTENT: " + nodes.keySet());
        for (Minute minute : nodes.values()) {
            minute.print();
        }
    }

    @Override
    public boolean hasRange(DateTime stDate, int length) {
        return false;
    }
}
