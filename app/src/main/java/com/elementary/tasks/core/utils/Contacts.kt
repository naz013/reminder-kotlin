package com.elementary.tasks.core.utils

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.ContactsContract
import androidx.annotation.RequiresPermission

object Contacts {

    @RequiresPermission(value = Permissions.READ_CONTACTS)
    fun pickContact(activity: Activity, code: Int) {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        activity.startActivityForResult(intent, code)
    }

    @RequiresPermission(value = Permissions.READ_CONTACTS)
    fun readPickerResults(context: Context, requestCode: Int, resultCode: Int, data: Intent?): Contact? {
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        val phoneNo = cursor.getString(cursor.getColumnIndex (ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val name = cursor.getString(cursor . getColumnIndex (ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        return Contact(name, phone = phoneNo)
                    }
                    cursor.close()
                }
            }
        }
        return null
    }

    @RequiresPermission(value = Permissions.READ_CONTACTS)
    fun getPhoto(contactId: Long): Uri? {
        if (contactId == 0L) {
            return null
        }
        val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
        return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
    }

    @RequiresPermission(value = Permissions.READ_CONTACTS)
    fun getIdFromNumber(phoneNumber: String?, context: Context): Long {
        if (phoneNumber == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) return 0
        var phoneContactID = 0L
        try {
            val contact = Uri.encode(phoneNumber)
            val contactLookupCursor = context.contentResolver
                    .query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
                            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID), null, null, null)
                    ?: return 0
            while (contactLookupCursor.moveToNext()) {
                phoneContactID = contactLookupCursor.getLong(
                        contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
            }
            contactLookupCursor.close()
        } catch (iae: IllegalArgumentException) {
            return 0
        }

        return phoneContactID
    }

    @RequiresPermission(value = Permissions.READ_CONTACTS)
    fun getIdFromMail(eMail: String?, context: Context): Long {
        if (eMail == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) return 0
        val uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(eMail))
        var contactId = 0L
        val contentResolver = context.contentResolver
        val contactLookup = contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
        contactLookup.use { look ->
            if (look != null && look.count > 0) {
                look.moveToNext()
                contactId = look.getLong(look.getColumnIndex(ContactsContract.PhoneLookup._ID))
            }
        }
        return contactId
    }

    @RequiresPermission(value = Permissions.READ_CONTACTS)
    fun getNameFromMail(eMail: String?, context: Context): String? {
        if (eMail == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) return null
        val uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(eMail))
        var name = "?"
        val contentResolver = context.contentResolver
        val contactLookup = contentResolver.query(uri, arrayOf(ContactsContract.Data.DISPLAY_NAME), null, null, null)
        contactLookup.use { look ->
            if (look != null && look.count > 0) {
                look.moveToNext()
                name = look.getString(look.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
            }
        }
        return name
    }

    @RequiresPermission(value = Permissions.READ_CONTACTS)
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

    @RequiresPermission(value = Permissions.READ_CONTACTS)
    fun getNumber(n: String?, context: Context): String {
        var name = n
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

    data class Contact(
            val name: String = "",
            val phone: String = "",
            val email: String = "",
            val photo: Uri? = null
    )
}
