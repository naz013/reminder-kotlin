package com.elementary.tasks.core.event_tree;

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
 *
 *
 * [0] year (if (-1) - get all nodes from tree);
 * [1] month (if (-1) - get all nodes from selected year);
 * [2] day (if (-1) - get all nodes from selected month);
 * [3] hour (if (-1) - get all nodes from selected day);
 * [4] minute (if (-1) - get all nodes from selected hour);
 */
public class Param {
    private int[] params;

    public Param(int[] params) {
        this.params = new int[params.length];
        System.arraycopy(params, 0, this.params, 0, this.params.length);
    }

    public int[] getParams() {
        return params;
    }

    public int getYear() {
        return params[0];
    }

    public int getMonth() {
        return params[1];
    }

    public int getDay() {
        return params[2];
    }

    public int getHour() {
        return params[3];
    }

    public int getMinute() {
        return params[4];
    }
}
