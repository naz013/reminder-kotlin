package com.elementary.tasks.core.data.models;

import com.elementary.tasks.core.utils.SuperUtil;

import java.util.UUID;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

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

@Entity
public class CalendarEvent {

    @PrimaryKey
    private String uuId;
    private int reminderId;
    private String event;
    private long eventId;

    public CalendarEvent(int reminderId, String event, long eventId) {
        this.uuId = UUID.randomUUID().toString();
        this.reminderId = reminderId;
        this.event = event;
        this.eventId = eventId;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return SuperUtil.getObjectPrint(this, CalendarEvent.class);
    }
}
