package com.elementary.tasks.voice;

import java.util.ArrayList;
import java.util.List;

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

class Container<V> {

    private List<V> list = new ArrayList<>();
    private V type;

    Container(List<V> list) {
        if (list == null) {
            return;
        }
        this.list.clear();
        this.list.addAll(list);
        if (!list.isEmpty()) {
            this.type = list.get(0);
        }
    }

    public V getType() {
        return type;
    }

    public List<V> getList() {
        return list;
    }

    public void setList(List<V> list) {
        this.list = list;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}
