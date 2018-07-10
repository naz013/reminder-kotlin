package com.elementary.tasks.core.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.ContactsContract

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

object Contacts {

    /**
     * Holder photo of contact.
     *
     * @param contactId contact identifier.
     * @return Contact photo
     */
    fun getPhoto(contactId: Long): Uri? {
        if (contactId == 0L) {
            return null
        }
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
    }

    /**
     * Holder contact identifier by contact phoneNumber.
     *
     * @param phoneNumber contact phoneNumber.
     * @param context     application context.
     * @return Contact identifier
     */
    fun getIdFromNumber(phoneNumber: String?, context: Context): Int {
        if (phoneNumber == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) return 0
        var phoneContactID = 0
        try {
            val contact = Uri.encode(phoneNumber)
            val contactLookupCursor = context.contentResolver
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID), null, null, null)
                    ?: return 0
            while (contactLookupCursor.moveToNext()) {
                phoneContactID = contactLookupCursor.getInt(
                        contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
            }
            contactLookupCursor.close()
        } catch (iae: IllegalArgumentException) {
            return 0
        }

        return phoneContactID
    }

    /**
     * Holder contact identifier by contact e-mail.
     *
     * @param eMail   contact e-mail.
     * @param context application context.
     * @return Contact identifier
     */
    fun getIdFromMail(eMail: String?, context: Context): Int {
        if (eMail == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) return 0
        val uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(eMail))
        var contactId = 0
        val contentResolver = context.contentResolver
        val contactLookup = contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
        try {
            if (contactLookup != null && contactLookup.count > 0) {
                contactLookup.moveToNext()
                contactId = contactLookup.getInt(contactLookup.getColumnIndex(ContactsContract.PhoneLookup._ID))
            }
        } finally {
            contactLookup?.close()
        }
        return contactId
    }

    /**
     * Holder contact name by contact e_mail.
     *
     * @param eMail   contact e-mail.
     * @param context application context.
     * @return Contact name
     */
    fun getNameFromMail(eMail: String?, context: Context): String? {
        if (eMail == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) return null
        val uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(eMail))
        var name = "?"
        val contentResolver = context.contentResolver
        val contactLookup = contentResolver.query(uri, arrayOf(ContactsContract.Data.DISPLAY_NAME), null, null, null)
        try {
            if (contactLookup != null && contactLookup.count > 0) {
                contactLookup.moveToNext()
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
            }
        } finally {
            contactLookup?.close()
        }
        return name
    }

    /**
     * Holder contact name by contact number.
     *
     * @param contactNumber contact number.
     * @param context       application context.
     * @return Contact name
     */
    fun getNameFromNumber(contactNumber: String?, context: Context): String? {
        var phoneContactID: String? = null
        if (contactNumber != null && Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
            try {
                val contact = Uri.encode(contactNumber)
                val contactLookupCursor = context.contentResolver.query(
                        Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                        arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID), null, null, null)
                        ?: return null
                while (contactLookupCursor.moveToNext()) {
                    phoneContactID = contactLookupCursor.getString(
                            contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                }
                contactLookupCursor.close()
            } catch (iae: IllegalArgumentException) {
                return phoneContactID
            }

        }
        return phoneContactID
    }

    /**
     * Holder contact number by contact name.
     *
     * @param name    contact name.
     * @param context application context.
     * @return Phone number
     */
    fun getNumber(name: String?, context: Context): String {
        var name = name
        var number = ""
        if (name != null && Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
            name = name.replace("'".toRegex(), "''")
        } else {
            return number
        }
        val selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + name + "%'"
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        try {
            val c = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, selection, null, null)
            if (c != null && c.moveToFirst()) {
                number = c.getString(0)
                c.close()
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }

        return number
    }
}
