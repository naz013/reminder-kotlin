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

class Year implements TreeInterface, MonthInterface {

    private int year;
    private int maxNodes = 11;
    private TreeMap<Integer, Month> nodes = new TreeMap<>();

    public Year(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    @Override
    public void buildTree(Param params, int position) {
        int month = params.getMonth();
        if (nodes.containsKey(month)) {
            Month m = nodes.get(month);
            m.buildTree(params, position);
        } else {
            Month m = new Month(month, this);
            m.buildTree(params, position);
            nodes.put(month, m);
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
        for (Month month : nodes.values()) {
            list.addAll(month.getAll());
        }
        return list;
    }

    @Override
    public List<Integer> getNodes(Param params) {
        int month = params.getMonth();
        if (nodes.containsKey(month)) {
            return nodes.get(month).getAll();
        }
        return null;
    }

    @Override
    public void remove(Param params, int position) {
        int month = params.getMonth();
        if (nodes.containsKey(month)) {
            Month m = nodes.get(month);
            m.remove(params, position);
            if (m.isEmpty()) {
                nodes.remove(m.getMonth());
            }
        }
    }


    @Override
    public void clearMinute(int year, int month, int day, int hour, int minute) {
        nodes.get(month).clearMinute(year, month, day, hour, minute);
    }

    @Override
    public void clearDay(int year, int month, int day) {
        nodes.get(month).clearDay(year, month, day);
    }

    @Override
    public void clearMonth(int year, int month) {
        nodes.put(month, new Month(month, this));
    }

    @Override
    public void clearHour(int year, int month, int day, int hour) {
        nodes.get(month).clearHour(year, month, day, hour);
    }

    @Override
    public void print() {
        System.out.println("YEAR -> " + year + ", CONTENT: " + nodes.keySet());
        for (Month month : nodes.values()) {
            month.print();
        }
    }
}
