package com.backdoor.simpleai;

import java.util.ArrayList;

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
public class Model {
    private String type, number, summary;
    private long dateTime, repeat;
    private Types types;
    private ArrayList<Integer> weekdays;
    private int activity;
    private int action;
    private boolean calendar;

    public Model() {

    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setCalendar(boolean calendar) {
        this.calendar = calendar;
    }

    public boolean getCalendar() {
        return calendar;
    }

    public ArrayList<Integer> getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(ArrayList<Integer> weekdays) {
        this.weekdays = weekdays;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public int getActivity() {
        return activity;
    }

    public long getRepeat() {
        return repeat;
    }

    public void setRepeat(long repeat) {
        this.repeat = repeat;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public long getDateTime() {
        return dateTime;
    }

    public String getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public Types getTypes() {
        return types;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTypes(Types types) {
        this.types = types;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
