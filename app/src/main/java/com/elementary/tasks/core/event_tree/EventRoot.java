package com.elementary.tasks.core.event_tree;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class EventRoot implements YearInterface, RootInterface {

    private Map<Integer, Year> nodes = new TreeMap<>();
    private List<Node> keys = new ArrayList<>();
    private Map<String, Integer> map = new HashMap<>();

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
    public int addNode(Object object) {
        EventInterface eventInterface = (EventInterface) object;
        if (map.containsKey(eventInterface.getUuId())) {
            int position = map.get(eventInterface.getUuId());
            addAnotherTree(position);
            return position;
        }
        Param param = new Param(eventInterface.getKeys());
        int year = param.getYear();
        keys.add(new Node(object, eventInterface.getUuId(), param));
        int position = keys.size() - 1;
        map.put(eventInterface.getUuId(), position);
        if (nodes.containsKey(year)) {
            nodes.get(year).buildTree(param, position);
        } else {
            Year y = new Year(year);
            y.buildTree(param, position);
            nodes.put(year, y);
        }
        return position;
    }

    private void addAnotherTree(int position) {

    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public List<Object> getNodes(int... params) {
        if (params.length == 0) return getAll();
        int y = params[0];
        if (y == -1) {
            return getAll();
        }
        if (nodes.containsKey(y)) {
            List<Integer> list = nodes.get(y).getNodes(new Param(params));
            if (list != null && !list.isEmpty()) {
                List<Object> objects = new ArrayList<>();
                for (int i : list) {
                    objects.add(keys.get(i).getObject());
                }
                return objects;
            }
        }
        return null;
    }

    @Override
    public Object getNode(int position) throws IndexOutOfBoundsException {
        if (position > 0 && position < size()) {
            return getNodes(keys.get(position).getKeys().getParams()).get(0);
        } else {
            throw new IndexOutOfBoundsException();
        }
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
    public void remove(Object o) {
        EventInterface eventInterface = (EventInterface) o;
        if (eventInterface.getUuId() == null) return;
        if (map.containsKey(eventInterface.getUuId())) {
            int position = map.get(eventInterface.getUuId());
            Node node = keys.get(position);
            for (Year year : nodes.values()) {
                year.remove(new Param(node.getKeys().getParams()), position);
            }
            map.remove(eventInterface.getUuId());
            keys.remove(position);
        }
    }

    @Override
    public void remove(int position) throws IndexOutOfBoundsException {
        if (position > 0 && position < size()) {
            remove(keys.get(position).getUuId());
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int indexOf(Object object) {
        EventInterface eventInterface = (EventInterface) object;
        if (map.containsKey(eventInterface.getUuId())) {
            return map.get(eventInterface.getUuId());
        }
        return -1;
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

    public void print() {
        System.out.println("ROOT -> CONTENT: " + nodes.keySet());
        for (Year year : nodes.values()) {
            year.print();
        }
    }
}
