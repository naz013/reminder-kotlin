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

class Day implements TreeInterface, HourInterface {

    private int day;
    private Month month;
    private TreeMap<Integer, Hour> nodes = new TreeMap<>();
    private int count = 0;

    Day(int day, Month month) {
        this.day = day;
        this.month = month;
        for (int i = 0; i < 24; i++) {
            nodes.put(i, new Hour(i, this));
        }
    }

    public int getDay() {
        return day;
    }

    public Month getMonth() {
        return month;
    }

    @Override
    public void addEvent(EventInterface eventInterface) {
        nodes.get(eventInterface.getHour()).addEvent(eventInterface);
        count++;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public List<EventInterface> getEvents(int y, int m, int d, int h, int min) {
        return nodes.get(h).getEvents(y, m, d, h, min);
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
