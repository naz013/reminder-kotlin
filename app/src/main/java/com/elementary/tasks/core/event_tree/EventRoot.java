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

public class EventRoot implements TreeInterface, YearInterface {

    private TreeMap<Integer, Year> nodes = new TreeMap<>();
    private int count;

    public EventRoot() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        if (!nodes.containsKey(year)) {
            Year y = new Year(year);
            nodes.put(year, y);
        }
    }

    @Override
    public void addNode(Object object) {
        EventInterface eventInterface = (EventInterface) object;
        int year = eventInterface.getYear();
        if (nodes.containsKey(year)) {
            nodes.get(year).addNode(eventInterface);
        } else {
            Year y = new Year(year);
            y.addNode(eventInterface);
            nodes.put(year, y);
        }
        count++;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public List<Object> getNodes(int... params) {
        if (params.length == 0) return getAll();
        int y = params[0];
        if (y == -1) {
            return getAll();
        }
        if (nodes.containsKey(y)) {
            return nodes.get(y).getNodes(params);
        } else return null;
    }

    @Override
    public List<Object> getAll() {
        List<Object> list = new ArrayList<>();
        for (Year year : nodes.values()) {
            list.addAll(year.getAll());
        }
        return list;
    }

    @Override
    public void remove(String uuId) {
        if (uuId == null) return;
        for (Year year : nodes.values()) {
            year.remove(uuId);
        }
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
}
