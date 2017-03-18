package com.elementary.tasks.core.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.ContactsContract;

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

public final class Contacts {

    private Context mContext;

    public Contacts(Context context) {
        this.mContext = context;
    }

    /**
     * Holder photo of contact.
     *
     * @param contactId contact identifier.
     * @return Contact photo
     */
    public static Uri getPhoto(long contactId) {
        if (contactId == 0) {
            return null;
        }
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
    }

    /**
     * Holder contact identifier by contact phoneNumber.
     *
     * @param phoneNumber contact phoneNumber.
     * @param context     application context.
     * @return Contact identifier
     */
    public static int getIdFromNumber(String phoneNumber, Context context) {
        int phoneContactID = 0;
        try {
            String contact = Uri.encode(phoneNumber);
            Cursor contactLookupCursor = context.getContentResolver()
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                            new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                            null, null, null);
            if (contactLookupCursor == null) {
                return 0;
            }
            while (contactLookupCursor.moveToNext()) {
                phoneContactID = contactLookupCursor.getInt(
                        contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            }
            contactLookupCursor.close();
        } catch (IllegalArgumentException iae) {
            return 0;
        }
        return phoneContactID;
    }

    /**
     * Holder contact identifier by contact e-mail.
     *
     * @param eMail   contact e-mail.
     * @param context application context.
     * @return Contact identifier
     */
    public static int getIdFromMail(String eMail, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(eMail));
        int contactId = 0;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[]{
                ContactsContract.PhoneLookup._ID}, null, null, null);
        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                contactId = contactLookup.getInt(contactLookup.getColumnIndex(ContactsContract.PhoneLookup._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        return contactId;
    }

    /**
     * Holder contact name by contact e_mail.
     *
     * @param eMail   contact e-mail.
     * @param context application context.
     * @return Contact name
     */
    public static String getNameFromMail(String eMail, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(eMail));
        String name = "?";
        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        return name;
    }

    /**
     * Holder contact name by contact number.
     *
     * @param contactNumber contact number.
     * @param context       application context.
     * @return Contact name
     */
    public static String getNameFromNumber(String contactNumber, Context context) {
        String phoneContactID = null;
        if (contactNumber != null) {
            try {
                String contact = Uri.encode(contactNumber);
                Cursor contactLookupCursor = context.getContentResolver().query(
                        Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                        new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                        null, null, null);
                if (contactLookupCursor == null) {
                    return null;
                }
                while (contactLookupCursor.moveToNext()) {
                    phoneContactID = contactLookupCursor.getString(
                            contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }
                contactLookupCursor.close();
            } catch (IllegalArgumentException iae) {
                return phoneContactID;
            }
        }
        return phoneContactID;
    }

    /**
     * Holder e=mail for contact.
     *
     * @param id contact identifier.
     * @return e-mail
     */
    public String getMail(int id) {
        String mail = null;
        if (id != 0) {
            ContentResolver cr = mContext.getContentResolver();
            Cursor emailCur = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    new String[]{String.valueOf(id)}, null);
            while (emailCur.moveToNext()) {
                mail = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            }
            emailCur.close();
        }
        return mail;
    }

    /**
     * Holder contact number by contact name.
     *
     * @param name    contact name.
     * @param context application context.
     * @return Phone number
     */
    public static String getNumber(String name, Context context) {
        String number = "";
        if (name != null) {
            name = name.replaceAll("'", "''");
        }
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + name + "%'";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        try {
            Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, selection, null, null);
            if (c != null && c.moveToFirst()) {
                number = c.getString(0);
                c.close();
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (number == null) {
            number = "noNumber";
        }
        return number;
    }
}
