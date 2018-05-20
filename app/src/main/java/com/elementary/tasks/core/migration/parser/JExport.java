package com.elementary.tasks.core.migration.parser;

import com.elementary.tasks.core.data.models.Reminder;

import org.json.JSONException;
import org.json.JSONObject;

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

class JExport {

    /**
     * JSON keys.
     */
    private static final String GTASKS = "g_tasks";
    private static final String CALENDAR = "to_calendar";
    private static final String CALENDAR_ID = "calendar_id";

    private int gTasks, calendar;
    private String calendarId;

    /**
     * JSON object.
     */
    private JSONObject jsonObject;

    void setExportData(Reminder item) {
        item.setExportToCalendar(calendar == 1);
        item.setExportToTasks(gTasks == 1);
    }

    /**
     * Get current JSON object.
     *
     * @return JSON object string
     */
    @Override
    public String toString() {
        return "JExport->Calendar: " + calendar +
                "->GTasks: " + gTasks +
                "->CalendarId: " + calendarId;
    }

    JExport(JSONObject jsonObject) {
        if (jsonObject != null) {
            this.jsonObject = jsonObject;
            parse(jsonObject);
        }
    }

    JExport() {
        jsonObject = new JSONObject();
        setGtasks(0);
        setCalendar(0);
        setCalendarId(null);
    }

    private void parse(JSONObject jsonObject) {
        if (jsonObject.has(CALENDAR_ID)) {
            try {
                calendarId = jsonObject.getString(CALENDAR_ID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(GTASKS)) {
            try {
                gTasks = jsonObject.getInt(GTASKS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.has(CALENDAR)) {
            try {
                calendar = jsonObject.getInt(CALENDAR);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get current JSON object.
     *
     * @return JSON object
     */
    JSONObject getJsonObject() {
        return jsonObject;
    }

    public JExport setCalendar(int calendar) {
        this.calendar = calendar;
        try {
            jsonObject.put(CALENDAR, calendar);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    private JExport setGtasks(int gTasks) {
        this.gTasks = gTasks;
        try {
            jsonObject.put(GTASKS, gTasks);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    private JExport setCalendarId(String calendarId) {
        this.calendarId = calendarId;
        try {
            jsonObject.put(CALENDAR_ID, calendarId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public int getCalendar() {
        return calendar;
    }
}
