package com.elementary.tasks.core.migration;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.elementary.tasks.core.migration.parser.JParser;
import com.elementary.tasks.core.migration.parser.JsonModel;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.List;

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

public class RemindersBase {

    private static final String DB_NAME = "reminder_base";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "reminders_table";

    public static final String _ID = "_id";
    public static final String SUMMARY = "summary";
    public static final String TYPE = "type";
    public static final String EVENT_TIME = "event_time";
    public static final String DELAY = "delay";
    public static final String CATEGORY = "category";
    public static final String JSON = "_json";
    public static final String DB_STATUS = "db_status";
    public static final String DB_LIST = "db_list";
    public static final String NOTIFICATION_STATUS = "n_status";
    public static final String UUID = "uuid";

    private DBHelper dbHelper;
    private Context mContext;
    private SQLiteDatabase db;

    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
//            sqLiteDatabase.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public RemindersBase(Context c) {
        mContext = c;
    }

    public RemindersBase open() throws SQLiteException {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        System.gc();
        return this;
    }

    public boolean isOpen() {
        return db != null && db.isOpen();
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private Reminder reminderFromCursor(Cursor c) {
        String summary = c.getString(c.getColumnIndex(SUMMARY));
        String json = c.getString(c.getColumnIndex(JSON));
        String type = c.getString(c.getColumnIndex(TYPE));
        String categoryId = c.getString(c.getColumnIndex(CATEGORY));
        String uuId = c.getString(c.getColumnIndex(UUID));
        int list = c.getInt(c.getColumnIndex(DB_LIST));
        int status = c.getInt(c.getColumnIndex(DB_STATUS));
        int notification = c.getInt(c.getColumnIndex(NOTIFICATION_STATUS));
        long id = c.getLong(c.getColumnIndex(_ID));
        long dateTime = c.getLong(c.getColumnIndex(EVENT_TIME));
        long delay = c.getLong(c.getColumnIndex(DELAY));
        Reminder item = new Reminder();
        item.setSummary(summary);
        item.setType(getType(type));
        item.setUuId(uuId);
        item.setGroupUuId(categoryId);
        item.setNotificationShown(notification == 1);
        item.setLocked(false);
        item.setEventTime(TimeUtil.getGmtFromDateTime(dateTime));
        item.setStartTime(TimeUtil.getGmtFromDateTime(dateTime));
        item.setDelay((int) (delay / (60 * 1000)));
        item.setActive(status == 0);
        item.setRemoved(list == 1);
        deleteReminder(id);
        JsonModel model = new JParser(json).parse();
        if (model != null) {
            model.setExtra(item);
        }
        return item;
    }

    private int getType(String type) {
        switch (type) {
            case "reminder":
                return Reminder.BY_DATE;
            case "day_of_month":
            case "day_of_month_last":
                return Reminder.BY_MONTH;
            case "day_of_month_call":
            case "day_of_month_call_last":
                return Reminder.BY_MONTH_CALL;
            case "day_of_month_message":
            case "day_of_month_message_last":
                return Reminder.BY_MONTH_SMS;
            case "time":
                return Reminder.BY_TIME;
            case "call":
                return Reminder.BY_DATE_CALL;
            case "e_mail":
                return Reminder.BY_DATE_EMAIL;
            case "shopping_list":
                return Reminder.BY_DATE_SHOP;
            case "message":
                return Reminder.BY_DATE_SMS;
            case "places_location":
                return Reminder.BY_PLACES;
            case "location":
                return Reminder.BY_LOCATION;
            case "out_location":
                return Reminder.BY_OUT;
            case "out_location_call":
                return Reminder.BY_OUT_CALL;
            case "out_location_message":
                return Reminder.BY_OUT_SMS;
            case "location_call":
                return Reminder.BY_LOCATION_CALL;
            case "location_message":
                return Reminder.BY_LOCATION_SMS;
            case "weekday":
                return Reminder.BY_WEEK;
            case "weekday_call":
                return Reminder.BY_WEEK_CALL;
            case "weekday_message":
                return Reminder.BY_WEEK_SMS;
            case "application":
                return Reminder.BY_DATE_APP;
            case "application_browser":
                return Reminder.BY_DATE_LINK;
            case "skype":
                return Reminder.BY_SKYPE_CALL;
            case "skype_chat":
                return Reminder.BY_SKYPE;
            case "skype_video":
                return Reminder.BY_SKYPE_VIDEO;
        }
        return Reminder.BY_DATE;
    }

    public List<Reminder> queryAllReminders() throws SQLException {
        openGuard();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
        List<Reminder> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(reminderFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        return list;
    }

    public boolean deleteReminder(long rowId) {
        return db.delete(TABLE_NAME, _ID + "=" + rowId, null) > 0;
    }

    public void openGuard() throws SQLiteException {
        if (isOpen()) return;
        open();
        if (isOpen()) return;
        throw new SQLiteException("Could not open database");
    }
}
