package com.elementary.tasks.core.event_tree;

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
    private int count = 0;

    @Override
    public void addEvent(EventInterface eventInterface) {
        int year = eventInterface.getYear();
        if (nodes.containsKey(year)) {
            nodes.get(year).addEvent(eventInterface);
        } else {
            Year y = new Year(year);
            y.addEvent(eventInterface);
            nodes.put(year, y);
        }
        count++;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public List<EventInterface> getEvents(int y, int m, int d, int h, int min) {
        if (nodes.containsKey(y)) {
            return nodes.get(y).getEvents(y, m, d, h, min);
        } else return null;
    }

    @Override
    public void remove(String uuId) {
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
