/**
 * Copyright 2016 Nazar Suhovich
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recurrence {

    private int dayOfMonth;
    private long repeatInterval, repeatLimit, after;
    private List<Integer> weekdays = new ArrayList<>();

    public Recurrence(int dayOfMonth, long repeatInterval, long repeatLimit, long after, List<Integer> weekdays) {
        this.dayOfMonth = dayOfMonth;
        this.repeatInterval = repeatInterval;
        this.repeatLimit = repeatLimit;
        this.after = after;
        this.weekdays = weekdays;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public long getRepeatLimit() {
        return repeatLimit;
    }

    public void setRepeatLimit(long repeatLimit) {
        this.repeatLimit = repeatLimit;
    }

    public long getAfter() {
        return after;
    }

    public void setAfter(long after) {
        this.after = after;
    }

    public List<Integer> getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(List<Integer> weekdays) {
        this.weekdays = weekdays;
    }

    @Override
    public String toString(){
        return "Recurrence->Month day: " + dayOfMonth +
                "->Repeat: " + repeatInterval +
                "->Limit: " + repeatLimit +
                "->After: " + after +
                "->Days: " + Arrays.asList(weekdays);
    }
}