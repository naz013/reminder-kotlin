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
    private int count;

    public Year(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    @Override
    public void addNode(Object object) {
        EventInterface eventInterface = (EventInterface) object;
        int month = eventInterface.getMonth();
        if (month < 0 || month > maxNodes) return;
        if (nodes.containsKey(month)) {
            nodes.get(month).addNode(object);
        } else {
            Month month1 = new Month(month, this);
            month1.addNode(object);
            nodes.put(month, month1);
        }
        count++;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public List<Object> getNodes(int... params) {
        if (params.length == 1) return getAll();
        int m = params[1];
        if (m == -1) {
            return getAll();
        }
        if (nodes.containsKey(m)) {
            return nodes.get(m).getNodes(params);
        } else return null;
    }

    @Override
    public List<Object> getAll() {
        List<Object> list = new ArrayList<>();
        for (Month month : nodes.values()) {
            list.addAll(month.getAll());
        }
        return list;
    }

    @Override
    public void remove(String uuId) {
        for (Month month : nodes.values()) {
            month.remove(uuId);
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
}
