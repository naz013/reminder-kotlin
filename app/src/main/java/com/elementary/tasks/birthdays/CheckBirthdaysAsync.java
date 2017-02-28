package com.elementary.tasks.birthdays;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.RealmDb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

public class CheckBirthdaysAsync extends AsyncTask<Void, Void, Integer> {

    private Context mContext;
    private final DateFormat[] birthdayFormats = {
            new SimpleDateFormat("yyyy-MM-dd", Locale.US),
            new SimpleDateFormat("yyyyMMdd", Locale.US),
            new SimpleDateFormat("yyyy.MM.dd", Locale.US),
            new SimpleDateFormat("yy.MM.dd", Locale.US),
            new SimpleDateFormat("MMM dd, yyyy", Locale.US),
            new SimpleDateFormat("yy/MM/dd", Locale.US),
    };
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private boolean showDialog = false;
    private ProgressDialog pd;
    private TaskCallback mCallback;

    public CheckBirthdaysAsync(Context context){
        this.mContext = context;
    }

    public CheckBirthdaysAsync(Context context, boolean showDialog){
        this.mContext = context;
        this.showDialog = showDialog;
        if (showDialog){
            pd = new ProgressDialog(context);
            pd.setMessage(context.getString(R.string.please_wait));
            pd.setCancelable(true);
        }
    }

    public CheckBirthdaysAsync(Context context, boolean showDialog, TaskCallback callback){
        this.mContext = context;
        this.showDialog = showDialog;
        this.mCallback = callback;
        if (showDialog){
            pd = new ProgressDialog(context);
            pd.setMessage(context.getString(R.string.please_wait));
            pd.setCancelable(true);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (showDialog) {
            pd.show();
        }
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ContentResolver cr = mContext.getContentResolver();
        int i = 0;
        String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        if (cur == null) {
            return 0;
        }
        while (cur.moveToNext()) {
            String contactId = cur.getString(cur.getColumnIndex(ContactsContract.Data._ID));
            String columns[] = {
                    ContactsContract.CommonDataKinds.Event.START_DATE,
                    ContactsContract.CommonDataKinds.Event.TYPE,
                    ContactsContract.CommonDataKinds.Event.MIMETYPE,
                    ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.Contacts._ID,
            };
            String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                    " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE +
                    "' and "                  + ContactsContract.Data.CONTACT_ID + " = " + contactId;
            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;
            List<BirthdayItem> contacts = RealmDb.getInstance().getAllBirthdays();
            Cursor birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder);
            if (birthdayCur != null && birthdayCur.getCount() > 0) {
                while (birthdayCur.moveToNext()) {
                    String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                    String name = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    int id = birthdayCur.getInt(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    String number = Contacts.getNumber(name, mContext);
                    Calendar calendar = Calendar.getInstance();
                    for (DateFormat f : birthdayFormats) {
                        Date date = null;
                        try {
                            date = f.parse(birthday);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (date != null) {
                            calendar.setTime(date);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            int month = calendar.get(Calendar.MONTH);
                            BirthdayItem birthdayItem = new BirthdayItem(name, DATE_FORMAT.format(calendar.getTime()), number, 0, id, day, month);
                            if (!contacts.contains(birthdayItem)) {
                                i = i + 1;
                            }
                            RealmDb.getInstance().saveObject(birthdayItem);
                            break;
                        }
                    }
                }
            }
            if (birthdayCur != null) {
                birthdayCur.close();
            }
        }
        cur.close();
        return i;
    }

    @Override
    protected void onPostExecute(Integer files) {
        if (showDialog) {
            try {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            } catch (final Exception e) {
            }
            if (files > 0) {
                Toast.makeText(mContext, files + " " +mContext.getString(R.string.events_found),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, R.string.found_nothing,
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (mCallback != null) {
            mCallback.onFinish();
        }
    }

    public interface TaskCallback {
        void onFinish();
    }
}
