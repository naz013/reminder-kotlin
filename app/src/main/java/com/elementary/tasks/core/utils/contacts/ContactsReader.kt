package com.elementary.tasks.core.utils.contacts

import android.content.ContentUris
import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.ContactsContract
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.io.readLong
import com.elementary.tasks.core.utils.io.readString

class ContactsReader(private val context: Context) {

  fun findNumber(query: String?): String? {
    if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS) || query == null) {
      return null
    }
    var part: String = query
    var number: String? = null

    while (part.length > 1) {
      val selection =
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'"
      val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
      val c = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection, selection, null, null
      )
      if (c != null && c.moveToFirst()) {
        number = c.getString(0)
        c.close()
      }
      if (number != null) {
        break
      }
      part = part.substring(0, part.length - 1)
    }
    return number
  }

  fun findEmail(query: String?): String? {
    if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS) || query == null) {
      return null
    }
    var part: String = query
    var email: String? = null

    while (part.length > 1) {
      val selection =
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'"
      val projection = arrayOf(ContactsContract.CommonDataKinds.Email.DATA)
      val c = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection, selection, null, null
      )
      if (c != null && c.moveToFirst()) {
        email = c.getString(0)
        c.close()
      }
      if (email != null)
        break
      part = part.substring(0, part.length - 2)
    }
    return email
  }

  fun getPhoto(contactId: Long): Uri? {
    if (contactId == 0L || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
      return null
    }
    val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
    return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
  }

  fun getIdFromNumber(phoneNumber: String?): Long {
    if (phoneNumber == null || !Permissions.checkPermission(
        context,
        Permissions.READ_CONTACTS
      )
    ) return 0
    var phoneContactID = 0L
    try {
      val contact = Uri.encode(phoneNumber)
      val contactLookupCursor = context.contentResolver
        .query(
          Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
          arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID),
          null,
          null,
          null
        )
        ?: return 0
      while (contactLookupCursor.moveToNext()) {
        phoneContactID = contactLookupCursor.getLong(
          contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)
        )
      }
      contactLookupCursor.close()
    } catch (iae: IllegalArgumentException) {
      return 0
    }

    return phoneContactID
  }

  fun getIdFromMail(eMail: String?): Long {
    if (eMail == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
      return 0
    }
    val uri = Uri.withAppendedPath(
      ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI,
      Uri.encode(eMail)
    )
    var contactId = 0L
    val contentResolver = context.contentResolver
    val contactLookup =
      contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
    contactLookup.use { look ->
      if (look != null && look.count > 0) {
        look.moveToNext()
        contactId = look.readLong(ContactsContract.PhoneLookup._ID) ?: 0
      }
    }
    return contactId
  }

  fun getNameFromMail(eMail: String?): String? {
    if (eMail == null || !Permissions.checkPermission(
        context,
        Permissions.READ_CONTACTS
      )
    ) return null
    val uri = Uri.withAppendedPath(
      ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI,
      Uri.encode(eMail)
    )
    var name = "?"
    val contentResolver = context.contentResolver
    val contactLookup =
      contentResolver.query(uri, arrayOf(ContactsContract.Data.DISPLAY_NAME), null, null, null)
    contactLookup.use { look ->
      if (look != null && look.count > 0) {
        look.moveToNext()
        name = look.readString(ContactsContract.Data.DISPLAY_NAME) ?: "?"
      }
    }
    return name
  }

  fun getNameFromNumber(contactNumber: String?): String? {
    var phoneContactID: String? = null
    if (contactNumber != null && Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
      try {
        val contact = Uri.encode(contactNumber)
        val contactLookupCursor = context.contentResolver.query(
          Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
          arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID),
          null,
          null,
          null
        )
          ?: return null
        while (contactLookupCursor.moveToNext()) {
          phoneContactID = contactLookupCursor.getString(
            contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
          )
        }
        contactLookupCursor.close()
      } catch (iae: IllegalArgumentException) {
        return phoneContactID
      }

    }
    return phoneContactID
  }

  fun getNumber(n: String?): String {
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
      val c = try {
        context.contentResolver.query(
          ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
          projection, selection, null, null
        )
      } catch (e: Exception) {
        null
      }
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