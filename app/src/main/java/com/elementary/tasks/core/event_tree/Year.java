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

class Year implements TreeInterface, MonthInterface {

    private int year;
    private TreeMap<Integer, Month> nodes = new TreeMap<>();
    private int count = 0;

    public Year(int year) {
        this.year = year;
        for (int i = 0; i < 12; i++) {
            nodes.put(i, new Month(i, this));
        }
    }

    public int getYear() {
        return year;
    }

    @Override
    public void addEvent(EventInterface eventInterface) {
        int month = eventInterface.getMonth();
        Month month1 = nodes.get(month);
        month1.addEvent(eventInterface);
        count++;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public List<EventInterface> getEvents(int y, int m, int d, int h, int min) {
        return nodes.get(m).getEvents(y, m, d, h, min);
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
