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

class Hour implements TreeInterface, MinuteInterface {

    private int hour;
    private Day day;
    private TreeMap<Integer, Minute> nodes = new TreeMap<>();
    private int count = 0;

    Hour(int hour, Day day) {
        this.day = day;
        this.hour = hour;
        for (int i = 0; i < 60; i++) {
            nodes.put(i, new Minute(i, this));
        }
    }

    public Day getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    @Override
    public void addEvent(EventInterface eventInterface) {
        nodes.get(eventInterface.getMinute()).addEvent(eventInterface);
        count++;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public List<EventInterface> getEvents(int y, int m, int d, int h, int min) {
        return nodes.get(min).getEvents(y, m, d, h, min);
    }

    @Override
    public void remove(String uuId) {
        for (Minute minute : nodes.values()) {
            minute.remove(uuId);
        }
    }

    @Override
    public void clearMinute(int year, int month, int day, int hour, int minute) {
        nodes.put(minute, new Minute(minute, this));
    }
}
