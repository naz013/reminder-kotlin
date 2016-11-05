package com.elementary.tasks.core.utils;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.Toast;

import com.elementary.tasks.R;

import java.util.ArrayList;
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

public class CalendarUtils {

    private Context mContext;
    private static CalendarUtils instance;

    private CalendarUtils(Context context){
        this.mContext = context;
    }

    public static CalendarUtils getInstance(Context context) {
        if (instance == null) {
            instance = new CalendarUtils(context);
        }
        return instance;
    }

    /**
     * Add event to calendar.
     * @param summary summary.
     * @param startTime start time of event in milliseconds.
     * @param id local reminder id.
     */
    public void addEvent(String summary, long startTime, long id){
        int mId = Prefs.getInstance(mContext).getCalendaId();
        if (mId != 0){
            TimeZone tz = TimeZone.getDefault();
            String timeZone = tz.getDisplayName();
            ContentResolver cr = mContext.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startTime);
            values.put(CalendarContract.Events.DTEND, startTime +
                    (60 * 1000 * Prefs.getInstance(mContext).getCalendarEventDuration()));
            if (summary != null) {
                values.put(CalendarContract.Events.TITLE, summary);
            }
            values.put(CalendarContract.Events.CALENDAR_ID, mId);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);
            values.put(CalendarContract.Events.ALL_DAY, 0);
            values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);
            values.put(CalendarContract.Events.DESCRIPTION, mContext.getString(R.string.from_reminder));
            Uri l_eventUri = Uri.parse("content://com.android.calendar/events");
            Uri event;
//            try {
//                event = cr.insert(l_eventUri, values);
//                DataBase db = new DataBase(mContext);
//                db.open();
//                if (event != null) {
//                    long eventID = Long.parseLong(event.getLastPathSegment());
//                    db.addCalendarEvent(event.toString(), id, eventID);
//                }
//                db.close();
//            } catch (Exception e) {
//                Toast.makeText(mContext, R.string.no_calendars_found, Toast.LENGTH_LONG).show();
//            }
        }
    }

    /**
     * Delete event from calendar.
     * @param id event identifier inside application.
     */
    public void deleteEvents(long id){
//        DataBase db = new DataBase(mContext);
//        db.open();
//        Cursor c = db.getCalendarEvents(id);
//        if (c != null && c.moveToFirst()){
//            do {
//                long mID = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
//                long eventId = c.getLong(c.getColumnIndex(Constants.COLUMN_EVENT_ID));
//                ContentResolver cr = mContext.getContentResolver();
//                cr.delete(CalendarContract.Events.CONTENT_URI,
//                        CalendarContract.Events._ID + "='" + eventId + "'", null);
//                db.deleteCalendarEvent(mID);
//            } while (c.moveToNext());
//        }
//        if (c != null) c.close();
//        db.close();
    }

    /**
     * Add event to stock Android calendar.
     * @param summary summary.
     * @param startTime event start time in milliseconds.
     */
    public void addEventToStock(String summary, long startTime){
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime +
                        (60 * 1000 * Prefs.getInstance(mContext).getCalendarEventDuration()))
                .putExtra(CalendarContract.Events.TITLE, summary)
                .putExtra(CalendarContract.Events.DESCRIPTION, mContext.getString(R.string.from_reminder));
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, R.string.stock_android_calendar_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get list of available Google calendars.
     * @return List of calendar identifiers.
     */
    public ArrayList<String> getCalendars() {
        ArrayList<String> ids = new ArrayList<>();
        ids.clear();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String[] mProjection = new String[] {
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(uri, mProjection, null, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
        if (c != null && c.moveToFirst()) {
            String mID;
            do {
                mID = c.getString(c.getColumnIndex(mProjection[0]));
                ids.add(mID);
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        if (ids.size() == 0) return null;
        else return ids;
    }

    /**
     * Get list of available Google calendars.
     * @return List of CalendarItem's.
     */
    public ArrayList<CalendarItem> getCalendarsList() {
        ArrayList<CalendarItem> ids = new ArrayList<>();
        ids.clear();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String[] mProjection = new String[] {
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(uri, mProjection, null, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
        if (c != null && c.moveToFirst()) {
            do {
                int mID = c.getInt(c.getColumnIndex(mProjection[0]));
                String title = c.getString(c.getColumnIndex(mProjection[2]));
                ids.add(new CalendarItem(title, mID));
            } while (c.moveToNext());
        }
        if (c != null) c.close();

        if (ids.size() == 0) return null;
        else return ids;
    }

    /**
     * Get list of events for calendar.
     * @param id calendar identifier.
     * @return List of EventItem's.
     */
    public ArrayList<EventItem> getEvents(int id) throws SecurityException {
        ArrayList<EventItem> list = new ArrayList<>();
        ContentResolver contentResolver = mContext.getContentResolver();
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
        if (c != null && c.moveToFirst()){
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
        }
        return list;
    }

    public class EventItem{
        private String title, description, rrule, rDate;
        private int calendarID, allDay;
        private long dtStart, dtEnd, id;

        public EventItem(String title, String description, String rrule, String rDate, int calendarID,
                         int allDay, long dtStart, long dtEnd, long id){
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

        public String getTitle(){
            return title;
        }

        public String getDescription(){
            return description;
        }

        public String getRrule(){
            return rrule;
        }

        public String getrDate(){
            return rDate;
        }

        public int getCalendarID(){
            return calendarID;
        }

        public int getAllDay(){
            return allDay;
        }

        public long getDtStart(){
            return dtStart;
        }

        public long getDtEnd(){
            return dtEnd;
        }

        public long getId(){
            return id;
        }
    }

    public class CalendarItem{
        private String name;
        private int id;

        public CalendarItem(String name, int id){
            this.name = name;
            this.id = id;
        }

        public String getName(){
            return name;
        }

        public int getId(){
            return id;
        }
    }
}
