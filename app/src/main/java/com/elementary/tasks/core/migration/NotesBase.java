package com.elementary.tasks.core.migration;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.notes.NoteImage;
import com.elementary.tasks.notes.NoteItem;

import java.util.ArrayList;
import java.util.Collections;
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

public class NotesBase {

    private static final String DB_NAME = "notes_base";
    private static final int DB_VERSION = 2;
    private static final String NOTE_TABLE_NAME = "notes_table";
    private DBHelper dbHelper;
    private Context mContext;
    private SQLiteDatabase db;

    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
//            sqLiteDatabase.execSQL(NOTE_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion){
                case 1:
                    db.execSQL("ALTER TABLE " + NOTE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_FONT_STYLE + " INTEGER");
                    db.execSQL("ALTER TABLE " + NOTE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_FONT_SIZE + " INTEGER");
                    db.execSQL("ALTER TABLE " + NOTE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_FONT_COLOR + " INTEGER");
                    db.execSQL("ALTER TABLE " + NOTE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_FONT_UNDERLINED + " INTEGER");
                    db.execSQL("ALTER TABLE " + NOTE_TABLE_NAME + " ADD COLUMN "
                            + Constants.COLUMN_LINK_ID + " INTEGER");
                    break;
            }
        }
    }

    public NotesBase(Context c) {
        mContext = c;
    }

    public NotesBase open() throws SQLiteException {
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
        if (dbHelper != null) dbHelper.close();
    }

    public List<NoteItem> getNotes() throws SQLException {
        openGuard();
        Cursor c = db.query(NOTE_TABLE_NAME, null, null, null, null, null, null);
        List<NoteItem> list = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                list.add(noteFromCursor(c));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    private NoteItem noteFromCursor(Cursor c) {
        String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
        int color  = c.getInt(c.getColumnIndex(Constants.COLUMN_COLOR));
        int style  = c.getInt(c.getColumnIndex(Constants.COLUMN_FONT_STYLE));
        byte[] image = c.getBlob(c.getColumnIndex(Constants.COLUMN_IMAGE));
        long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
        NoteItem item = new NoteItem();
        item.setColor(color);
        item.setStyle(style);
        item.setDate(TimeUtil.getGmtDateTime());
        item.setSummary(note);
        if (image != null) {
            item.setImages(Collections.singletonList(new NoteImage(image)));
        }
        deleteNote(id);
        return item;
    }

    private void deleteNote(long rowId) {
        db.delete(NOTE_TABLE_NAME, Constants.COLUMN_ID + "=" + rowId, null);
    }

    private void openGuard() throws SQLiteException {
        if(isOpen()) return;
        open();
        if(isOpen()) return;
        throw new SQLiteException("Could not open database");
    }
}
