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

class Hour implements TreeInterface, MinuteInterface {

    private int hour;
    private int maxNodes = 59;
    private Day day;
    private TreeMap<Integer, Minute> nodes = new TreeMap<>();
    private int count = 0;

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
    public void addNode(Object object) {
        EventInterface eventInterface = (EventInterface) object;
        int min = eventInterface.getMinute();
        if (min < 0 || min > maxNodes) return;
        if (nodes.containsKey(min)) {
            nodes.get(min).addNode(object);
        } else {
            Minute minute = new Minute(min, this);
            minute.addNode(object);
            nodes.put(min, minute);
        }
        count++;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public List<Object> getNodes(int... params) {
        if (params.length == 4) return getAll();
        int min = params[4];
        if (min == -1) return getAll();
        if (nodes.containsKey(min)) {
            return nodes.get(min).getNodes(params);
        } else return null;
    }

    @Override
    public List<Object> getAll() {
        List<Object> list = new ArrayList<>();
        for (Minute minute : nodes.values()) {
            list.addAll(minute.getAll());
        }
        return list;
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
