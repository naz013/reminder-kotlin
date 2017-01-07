package com.elementary.tasks.core.event_tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, Object> nodes = new HashMap<>();

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
    public void addNode(Object object) {
        EventInterface eventInterface = (EventInterface) object;
        nodes.put(eventInterface.getUuId(), object);
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public List<Object> getNodes(int... params) {
        return getAll();
    }

    @Override
    public List<Object> getAll() {
        List<Object> list = new ArrayList<>();
        list.addAll(nodes.values());
        return list;
    }

    @Override
    public void remove(String uuId) {
        if (nodes.containsKey(uuId)) {
            nodes.remove(uuId);
        }
    }
}
