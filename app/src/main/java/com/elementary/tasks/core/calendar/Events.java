package com.elementary.tasks.core.calendar;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

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

public class Events {
    private ArrayList<Event> events;
    private int mPosition = 0;

    public Events() {
        events = new ArrayList<>();
    }

    public Events(Event event) {
        events = new ArrayList<>();
        events.add(event);
    }

    public Events(String task, int color, Type type, long time) {
        Event event = new Event(task, color, type, time);
        if (events != null) {
            events.add(event);
        } else {
            events = new ArrayList<>();
            events.add(event);
        }
        Collections.sort(events, (event1, t1) -> (int) (event1.getTime() - t1.getTime()));
    }

    public void moveToStart() {
        mPosition = 0;
    }

    public int addEvent(String task, int color, Type type, long time) {
        Event event = new Event(task, color, type, time);
        if (events != null) {
            events.add(event);
        } else {
            events = new ArrayList<>();
            events.add(event);
        }
        return events.indexOf(event);
    }

    @Nullable
    public Event getNextWithoutMoving() {
        int index = mPosition + 1;
        if (events != null && index < events.size()) {
            return events.get(index);
        } else return null;
    }

    @Nullable
    public Event getPreviousWithoutMoving() {
        if (mPosition == 0) return null;
        int index = mPosition - 1;
        if (events != null && index < events.size()) {
            return events.get(index);
        } else return null;
    }

    public Event getNext() {
        if (events != null && mPosition < events.size()) {
            Event event = events.get(mPosition);
            mPosition++;
            return event;
        } else return null;
    }

    public Event getLast() {
        if (events != null) {
            return events.get(events.size() - 1);
        } else return null;
    }

    public boolean hasNext() {
        return events != null && mPosition < events.size();
    }

    public int count() {
        return events.size();
    }

    @Override
    public String toString() {
        return events.toString();
    }

    public enum Type {
        REMINDER,
        BIRTHDAY
    }

    public static class Event {
        private String task;
        private int color;
        private Type type;
        private long time;

        public Event(String task, int color, Type type, long time) {
            this.task = task;
            this.color = color;
            this.type = type;
            this.time = time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public String getTask() {
            return task;
        }

        public void setTask(String task) {
            this.task = task;
        }

        @Override
        public String toString() {
            return "Event: task " + task + "" +
                    " || color: " + color + " || type " + type;
        }
    }
}
