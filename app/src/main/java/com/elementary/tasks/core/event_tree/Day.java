package com.elementary.tasks.core.event_tree;

import java.util.ArrayList;
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

class Day implements TreeInterface, HourInterface {

    private int day;
    private int maxNodes = 23;
    private Month month;
    private TreeMap<Integer, Hour> nodes = new TreeMap<>();

    Day(int day, Month month) {
        this.day = day;
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public Month getMonth() {
        return month;
    }

    @Override
    public void buildTree(Param params, int position) {
        int hour = params.getHour();
        if (nodes.containsKey(hour)) {
            Hour h = nodes.get(hour);
            h.buildTree(params, position);
        } else {
            Hour h = new Hour(hour, this);
            h.buildTree(params, position);
            nodes.put(hour, h);
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
        for (Hour hour : nodes.values()) {
            list.addAll(hour.getAll());
        }
        return list;
    }

    @Override
    public List<Integer> getNodes(Param params) {
        int hour = params.getHour();
        if (nodes.containsKey(hour)) {
            return nodes.get(hour).getAll();
        }
        return null;
    }

    @Override
    public void remove(Param params, int position) {
        int hour = params.getHour();
        if (nodes.containsKey(hour)) {
            Hour h = nodes.get(hour);
            h.remove(params, position);
            if (h.isEmpty()) {
                nodes.remove(h.getHour());
            }
        }
    }


    @Override
    public void clearMinute(int year, int month, int day, int hour, int minute) {
        nodes.get(hour).clearMinute(year, month, day, hour, minute);
    }

    @Override
    public void clearHour(int year, int month, int day, int hour) {
        nodes.put(hour, new Hour(hour, this));
    }

    @Override
    public void print() {
        System.out.println("DAY -> " + day + ", CONTENT: " + nodes.keySet());
        for (Hour hour : nodes.values()) {
            hour.print();
        }
    }
}
