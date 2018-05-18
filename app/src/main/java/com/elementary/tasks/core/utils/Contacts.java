package com.elementary.tasks.core.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private Contacts() {
    }

    /**
     * Holder photo of contact.
     *
     * @param contactId contact identifier.
     * @return Contact photo
     */
    @Nullable
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
    public static int getIdFromNumber(@Nullable String phoneNumber, Context context) {
        if (phoneNumber == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) return 0;
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
    public static int getIdFromMail(@Nullable String eMail, Context context) {
        if (eMail == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) return 0;
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
    @Nullable
    public static String getNameFromMail(@Nullable String eMail, Context context) {
        if (eMail == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) return null;
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
    @Nullable
    public static String getNameFromNumber(@Nullable String contactNumber, Context context) {
        String phoneContactID = null;
        if (contactNumber != null && Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
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
     * Holder contact number by contact name.
     *
     * @param name    contact name.
     * @param context application context.
     * @return Phone number
     */
    @NonNull
    public static String getNumber(@Nullable String name, Context context) {
        String number = "";
        if (name != null && Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
            name = name.replaceAll("'", "''");
        } else {
            return number;
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
        return number;
    }
}
