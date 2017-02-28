package com.elementary.tasks.core.utils;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.widget.Toast;

import com.backdoor.simpleai.ObjectUtil;
import com.elementary.tasks.R;
import com.elementary.tasks.core.calendar.CalendarEvent;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

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

public final class CalendarUtils {

    private static final String TAG = "CalendarUtils";

    private CalendarUtils() {}

    /**
     * Add event to calendar.
     */
    public static void addEvent(Context context, Reminder reminder) {
        int mId = Prefs.getInstance(context).getCalendarId();
        if (mId != 0) {
            TimeZone tz = TimeZone.getDefault();
            String timeZone = tz.getDisplayName();
            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            long startTime = TimeUtil.getDateTimeFromGmt(reminder.getEventTime());
            values.put(CalendarContract.Events.DTSTART, startTime);
            values.put(CalendarContract.Events.DTEND, startTime +
                    (60 * 1000 * Prefs.getInstance(context).getCalendarEventDuration()));
            if (!TextUtils.isEmpty(reminder.getSummary())) {
                values.put(CalendarContract.Events.TITLE, reminder.getSummary());
            }
            values.put(CalendarContract.Events.CALENDAR_ID, mId);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);
            values.put(CalendarContract.Events.ALL_DAY, 0);
            values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);
            values.put(CalendarContract.Events.DESCRIPTION, context.getString(R.string.from_reminder));
            Uri lEventUri = Uri.parse("content://com.android.calendar/events");
            Uri event;
            try {
                event = cr.insert(lEventUri, values);
                if (event != null) {
                    long eventID = Long.parseLong(event.getLastPathSegment());
                    RealmDb.getInstance().saveObject(new CalendarEvent(reminder.getUuId(), event.toString(), eventID));
                }
            } catch (Exception e) {
                Toast.makeText(context, R.string.no_calendars_found, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Delete event from calendar.
     *
     * @param id event identifier inside application.
     */
    @SuppressWarnings("MissingPermission")
    public static void deleteEvents(Context context, String id) {
        List<CalendarEvent> events = RealmDb.getInstance().getCalendarEvents(id);
        ContentResolver cr = context.getContentResolver();
        for (int i = events.size() - 1; i >= 0; i--) {
            CalendarEvent event = events.remove(i);
            cr.delete(CalendarContract.Events.CONTENT_URI,
                    CalendarContract.Events._ID + "='" + event.getEventId() + "'", null);
            RealmDb.getInstance().deleteCalendarEvent(event);
        }
    }

    /**
     * Add event to stock Android calendar.
     *
     * @param summary   summary.
     * @param startTime event start time in milliseconds.
     */
    public static void addEventToStock(Context context, String summary, long startTime) {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime +
                        (60 * 1000 * Prefs.getInstance(context).getCalendarEventDuration()))
                .putExtra(CalendarContract.Events.TITLE, summary)
                .putExtra(CalendarContract.Events.DESCRIPTION, context.getString(R.string.from_reminder));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.stock_android_calendar_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Holder list of available Google calendars.
     *
     * @return List of calendar identifiers.
     */
    public static List<String> getCalendars(Context context) {
        List<String> ids = new ArrayList<>();
        ids.clear();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String[] mProjection = new String[]{
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };
        Cursor c = null;
        try {
            c = context.getContentResolver().query(uri, mProjection, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (c != null && c.moveToFirst()) {
            String mID;
            do {
                mID = c.getString(c.getColumnIndex(mProjection[0]));
                ids.add(mID);
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        if (ids.size() == 0) {
            return null;
        } else {
            return ids;
        }
    }

    /**
     * Holder list of available Google calendars.
     *
     * @return List of CalendarItem's.
     */
    public static List<CalendarItem> getCalendarsList(Context context) {
        List<CalendarItem> ids = new ArrayList<>();
        ids.clear();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String[] mProjection = new String[]{
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };
        Cursor c = null;
        try {
            c = context.getContentResolver().query(uri, mProjection, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (c != null && c.moveToFirst()) {
            do {
                int mID = c.getInt(c.getColumnIndex(mProjection[0]));
                String title = c.getString(c.getColumnIndex(mProjection[2]));
                ids.add(new CalendarItem(title, mID));
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        if (ids.size() == 0) {
            return null;
        } else {
            return ids;
        }
    }

    /**
     * Holder list of events for calendar.
     *
     * @param id calendar identifier.
     * @return List of EventItem's.
     */
    public static List<EventItem> getEvents(Context context, int id) throws SecurityException {
        List<EventItem> list = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor c = contentResolver.query(CalendarContract.Events.CONTENT_URI,
                new String[]{CalendarContract.Events.TITLE,
                        CalendarContract.Events.DESCRIPTION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND,
                        CalendarContract.Events.RRULE,
                        CalendarContract.Events.RDATE,
                        CalendarContract.Events._ID,
                        CalendarContract.Events.CALENDAR_ID,
                        CalendarContract.Events.ALL_DAY},
                CalendarContract.Events.CALENDAR_ID + "='" + id + "'",
                null, "dtstart ASC");
        if (c != null && c.moveToFirst()) {
            do {
                String title = c.getString(c.getColumnIndex(CalendarContract.Events.TITLE));
                String description = c.getString(c.getColumnIndex(CalendarContract.Events.DESCRIPTION));
                String rrule = c.getString(c.getColumnIndex(CalendarContract.Events.RRULE));
                String rDate = c.getString(c.getColumnIndex(CalendarContract.Events.RDATE));
                int calendarId = c.getInt(c.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
                int allDay = c.getInt(c.getColumnIndex(CalendarContract.Events.ALL_DAY));
                long dtStart = c.getLong(c.getColumnIndex(CalendarContract.Events.DTSTART));
                long dtEnd = c.getLong(c.getColumnIndex(CalendarContract.Events.DTEND));
                long eventID = c.getLong(c.getColumnIndex(CalendarContract.Events._ID));
                list.add(new EventItem(title, description, rrule, rDate,
                        calendarId, allDay, dtStart, dtEnd, eventID));
            } while (c.moveToNext());
            c.close();
        }
        return list;
    }

    public static class EventItem {
        private String title, description, rrule, rDate;
        private int calendarID, allDay;
        private long dtStart, dtEnd, id;

        public EventItem(String title, String description, String rrule, String rDate, int calendarID,
                         int allDay, long dtStart, long dtEnd, long id) {
            this.title = title;
            this.description = description;
            this.rrule = rrule;
            this.rDate = rDate;
            this.calendarID = calendarID;
            this.allDay = allDay;
            this.dtStart = dtStart;
            this.dtEnd = dtEnd;
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getRrule() {
            return rrule;
        }

        public String getrDate() {
            return rDate;
        }

        public int getCalendarID() {
            return calendarID;
        }

        public int getAllDay() {
            return allDay;
        }

        public long getDtStart() {
            return dtStart;
        }

        public long getDtEnd() {
            return dtEnd;
        }

        public long getId() {
            return id;
        }

        @Override
        public String toString() {
            return ObjectUtil.getObjectPrint(this, EventItem.class);
        }
    }

    public static class CalendarItem {
        private String name;
        private int id;

        public CalendarItem(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return ObjectUtil.getObjectPrint(this, CalendarItem.class);
        }
    }
}
