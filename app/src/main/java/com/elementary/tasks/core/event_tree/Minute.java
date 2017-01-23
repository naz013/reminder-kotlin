package com.elementary.tasks.core.event_tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

class Minute implements TreeInterface {

    private int minute;
    private Hour hour;
    private Set<Integer> nodes = new TreeSet<>();

    public Minute(int minute, Hour hour) {
        this.minute = minute;
        this.hour = hour;
    }

    public Hour getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }


    @Override
    public void buildTree(Param params, int position) {
        nodes.add(position);

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
        return new ArrayList<>(nodes);
    }

    @Override
    public List<Integer> getNodes(Param params) {
        return getAll();
    }

    @Override
    public void remove(Param params, int position) {
        if (nodes.contains(position)) {
            nodes.remove(position);
        }
    }

    @Override
    public void print() {
        System.out.println("MINUTE -> " + minute + ", CONTENT: " + nodes.size());
    }
}
