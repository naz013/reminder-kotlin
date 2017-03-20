package com.elementary.tasks.core.migration;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.navigation.settings.additional.TemplateItem;

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

class DataBase {

    private static final String DB_NAME = "just_database";
    private static final int DB_VERSION = 14;
    private static final String CONTACTS_TABLE_NAME = "contacts_task_table";
    private static final String NOTE_TABLE_NAME = "notes_table";
    private static final String MESSAGES_TABLE_NAME = "messages_table";
    private static final String CATEGORIES_TABLE_NAME = "categories_table";

    private DBHelper dbHelper;
    private Context mContext;
    private SQLiteDatabase db;

    private static final String CONTACTS_TABLE_CREATE =
            "create table " + CONTACTS_TABLE_NAME + "(" +
                    Constants.Contacts.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.Contacts.COLUMN_NAME + " VARCHAR(255), " +
                    Constants.Contacts.COLUMN_CONTACT_ID + " INTEGER, " +
                    Constants.Contacts.COLUMN_NUMBER + " VARCHAR(255), " +
                    Constants.Contacts.COLUMN_CONTACT_MAIL + " VARCHAR(255), " +
                    Constants.Contacts.COLUMN_BIRTHDATE + " VARCHAR(255), " +
                    Constants.Contacts.COLUMN_DAY + " INTEGER, " +
                    Constants.Contacts.COLUMN_MONTH + " INTEGER, " +
                    Constants.Contacts.COLUMN_UUID + " VARCHAR(255), " +
                    Constants.Contacts.COLUMN_VAR + " VARCHAR(255) " +
                    ");";

    private static final String MESSAGES_TABLE_CREATE =
            "create table " + MESSAGES_TABLE_NAME + "(" +
                    Constants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.COLUMN_TEXT + " VARCHAR(255), " +
                    Constants.COLUMN_DATE_TIME + " INTEGER " +
                    ");";

    private static final String CATEGORIES_TABLE_CREATE =
            "create table " + CATEGORIES_TABLE_NAME + "(" +
                    Constants.COLUMN_ID + " integer primary key autoincrement, " +
                    Constants.COLUMN_TEXT + " VARCHAR(255), " +
                    Constants.COLUMN_COLOR + " INTEGER, " +
                    Constants.COLUMN_TECH_VAR + " VARCHAR(255), " +
                    Constants.COLUMN_CATEGORY + " VARCHAR(255), " +
                    Constants.COLUMN_FEATURE_TIME + " INTEGER, " +
                    Constants.COLUMN_DELAY + " INTEGER, " +
                    Constants.COLUMN_DATE_TIME + " INTEGER " +
                    ");";

    private class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion){
                case 1:
                    db.execSQL(CONTACTS_TABLE_CREATE);
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
                case 2:
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
                case 3:
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
                case 4:
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    db.execSQL("DELETE FROM " + NOTE_TABLE_NAME);
                    break;
                case 5:
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
                case 6:
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
                case 7:
                    db.execSQL(MESSAGES_TABLE_CREATE);
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
                case 8:
                    db.execSQL(CATEGORIES_TABLE_CREATE);
                    break;
            }
        }
    }

    DataBase(Context c) {
        mContext = c;
    }

    DataBase open() throws SQLiteException {
        dbHelper = new DBHelper(mContext);
        db = dbHelper.getWritableDatabase();
        System.gc();
        return this;
    }

    boolean isOpen() {
        return db != null && db.isOpen();
    }

    void close() {
        if (dbHelper != null) dbHelper.close();
    }

    private boolean deleteBirthday(long rowId) {
        openGuard();
        return db.delete(CONTACTS_TABLE_NAME, Constants.Contacts.COLUMN_ID + "=" + rowId, null) > 0;
    }

    List<BirthdayItem> getBirthdays() throws SQLException {
        openGuard();
        Cursor c = db.query(CONTACTS_TABLE_NAME, null, null, null, null, null, null);
        List<BirthdayItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(birthdayFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    private BirthdayItem birthdayFromCursor(Cursor c) {
        String name = c.getString(c.getColumnIndex(Constants.Contacts.COLUMN_NAME));
        String date = c.getString(c.getColumnIndex(Constants.Contacts.COLUMN_BIRTHDATE));
        String number = c.getString(c.getColumnIndex(Constants.Contacts.COLUMN_NUMBER));
        String shownYear = c.getString(c.getColumnIndex(Constants.Contacts.COLUMN_VAR));
        int conId = c.getInt(c.getColumnIndex(Constants.Contacts.COLUMN_CONTACT_ID));
        int day = c.getInt(c.getColumnIndex(Constants.Contacts.COLUMN_DAY));
        int month = c.getInt(c.getColumnIndex(Constants.Contacts.COLUMN_MONTH));
        long id = c.getLong(c.getColumnIndex(Constants.Contacts.COLUMN_ID));
        deleteBirthday(id);
        int year = 0;
        try {
            year = Integer.parseInt(shownYear);
        } catch (NumberFormatException ignored) {
        }
        return new BirthdayItem(name, date, number, year, conId, day, month);
    }

    List<TemplateItem> getAllTemplates() throws SQLException {
        openGuard();
        List<TemplateItem> list = new ArrayList<>();
        Cursor c = db.query(MESSAGES_TABLE_NAME, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
                long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                deleteTemplate(id);
                list.add(new TemplateItem(title));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    private boolean deleteTemplate(long rowId) {
        openGuard();
        return db.delete(MESSAGES_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    List<GroupItem> getAllGroups() throws SQLException {
        openGuard();
        List<GroupItem> list = new ArrayList<>();
        Cursor c = db.query(CATEGORIES_TABLE_NAME, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(groupFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    private GroupItem groupFromCursor(Cursor c) {
        String text = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
        int color = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
        long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
        deleteGroup(id);
        return new GroupItem(text, color);
    }

    private boolean deleteGroup(long rowId) {
        openGuard();
        return db.delete(CATEGORIES_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null) > 0;
    }

    private void openGuard() throws SQLiteException {
        if(isOpen()) return;
        open();
        if(isOpen()) return;
        throw new SQLiteException("Could not open database");
    }
}
