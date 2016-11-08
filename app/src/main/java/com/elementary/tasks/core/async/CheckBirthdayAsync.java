package com.elementary.tasks.core.async;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.elementary.tasks.R;

import java.text.SimpleDateFormat;
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

public class CheckBirthdayAsync extends AsyncTask<Void, Void, Integer> {

    private Context mContext;
    private final SimpleDateFormat[] birthdayFormats = {
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            new SimpleDateFormat("yyyyMMdd", Locale.getDefault()),
            new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()),
            new SimpleDateFormat("yy.MM.dd", Locale.getDefault()),
            new SimpleDateFormat("yy/MM/dd", Locale.getDefault()),
    };

    private boolean showDialog = false;
    private ProgressDialog pd;

    public CheckBirthdayAsync(Context context){
        this.mContext = context;
    }

    public CheckBirthdayAsync(Context context, boolean showDialog){
        this.mContext = context;
        this.showDialog = showDialog;
        if (showDialog){
            pd = new ProgressDialog(context);
            pd.setMessage(context.getString(R.string.please_wait));
            pd.setCancelable(true);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (showDialog) pd.show();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ContentResolver cr = mContext.getContentResolver();
        int i = 0;
        String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
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
            String[] selectionArgs = null;
            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;
//            List<Integer> contacts = BirthdayHelper.getInstance(mContext).getContacts();
//            Cursor birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, selectionArgs, sortOrder);
//            if (birthdayCur != null && birthdayCur.getCount() > 0) {
//                while (birthdayCur.moveToNext()) {
//                    Date date;
//                    String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
//                    String name = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
//                    int id = birthdayCur.getInt(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
//                    String number = com.cray.software.justreminder.contacts.Contacts.getNumber(name, mContext);
//                    Calendar calendar = Calendar.getInstance();
//                    for (SimpleDateFormat f : birthdayFormats) {
//                        try {
//                            date = f.parse(birthday);
//                            if (date != null) {
//                                calendar.setTime(date);
//                                int day = calendar.get(Calendar.DAY_OF_MONTH);
//                                int month = calendar.get(Calendar.MONTH);
//                                if (!contacts.contains(id)) {
//                                    i = i + 1;
//                                    BirthdayItem item = new BirthdayItem(0, name, birthday, number, SyncHelper.generateID(), null, id, day, month);
//                                    BirthdayHelper.getInstance(mContext).saveBirthday(item);
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//            if (birthdayCur != null) birthdayCur.close();
        }
        cur.close();
        return i;
    }

    @Override
    protected void onPostExecute(Integer files) {
        if (showDialog) {
            try {
                if ((pd != null) && pd.isShowing()) {
                    pd.dismiss();
                }
            } catch (final Exception e) {
                // Handle or log or ignore
            }
            if (files > 0) {
                Toast.makeText(mContext, files + " " +mContext.getString(R.string.events_found), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, R.string.found_nothing, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
