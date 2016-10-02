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

public class Export {

    private boolean exportToTasks, exportToCalendar;
    private String calendarId;

    public Export(boolean exportToTasks, boolean exportToCalendar, String calendarId) {
        this.exportToTasks = exportToTasks;
        this.exportToCalendar = exportToCalendar;
        this.calendarId = calendarId;
    }

    public boolean isExportToTasks() {
        return exportToTasks;
    }

    public void setExportToTasks(boolean exportToTasks) {
        this.exportToTasks = exportToTasks;
    }

    public boolean isExportToCalendar() {
        return exportToCalendar;
    }

    public void setExportToCalendar(boolean exportToCalendar) {
        this.exportToCalendar = exportToCalendar;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    @Override
    public String toString(){
        return "Export->Calendar: " + exportToCalendar +
                "->GTasks: " + exportToTasks +
                "->CalendarId: " + calendarId;
    }
}
