/*
 * Copyright 2015 Nazar Suhovich
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

package com.elementary.tasks.reminder.models;

import java.util.Arrays;
import java.util.List;

public class Exclusion {

    private String from, to;
    private List<Integer> hours;

    public Exclusion(String from, String to, List<Integer> hours) {
        this.from = from;
        this.to = to;
        this.hours = hours;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<Integer> getHours() {
        return hours;
    }

    public void setHours(List<Integer> hours) {
        this.hours = hours;
    }

    @Override
    public String toString() {
        return "JExclusion->From: " + from +
                "->To: " + to +
                "->Hours: " + Arrays.asList(hours);
    }
}
