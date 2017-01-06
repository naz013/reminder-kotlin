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
    private int count = 0;

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
    public void addNode(Object object) {
        EventInterface eventInterface = (EventInterface) object;
        int hour = eventInterface.getHour();
        if (hour < 0 || hour > maxNodes) return;
        if (nodes.containsKey(hour)) {
            nodes.get(hour).addNode(object);
        } else {
            Hour hour1 = new Hour(hour, this);
            hour1.addNode(object);
            nodes.put(hour, hour1);
        }
        count++;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public List<Object> getNodes(int... params) {
        if (params.length == 3) return getAll();
        int h = params[3];
        if (h == -1) return getAll();
        if (nodes.containsKey(h)) {
            return nodes.get(h).getNodes(params);
        } else return null;
    }

    @Override
    public List<Object> getAll() {
        List<Object> list = new ArrayList<>();
        for (Hour hour : nodes.values()) {
            list.addAll(hour.getAll());
        }
        return list;
    }

    @Override
    public void remove(String uuId) {
        for (Hour hour : nodes.values()) {
            hour.remove(uuId);
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
}
