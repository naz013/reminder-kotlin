package com.github.naz013.common.contacts

import android.content.ContentUris
import android.content.Context
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import com.github.naz013.common.Permissions
import com.github.naz013.feature.common.readLong
import com.github.naz013.feature.common.readString

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
        /* uri = */ ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        /* projection = */ projection,
        /* selection = */ selection,
        /* selectionArgs = */ null,
        /* sortOrder = */ null
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
        /* uri = */ ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        /* projection = */ projection,
        /* selection = */ selection,
        /* selectionArgs = */ null,
        /* sortOrder = */ null
      )
      if (c != null && c.moveToFirst()) {
        email = c.getString(0)
        c.close()
      }
      if (email != null) {
        break
      }
      part = part.substring(0, part.length - 2)
    }
    return email
  }

  fun getPhotoBitmap(contactId: Long): Bitmap? {
    if (contactId == 0L) return null
    try {
      val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
      val input =
        ContactsContract.Contacts.openContactPhotoInputStream(context.contentResolver, uri)
          ?: return null
      return BitmapFactory.decodeStream(input)
    } catch (e: Throwable) {
      return null
    }
  }

  fun getIdFromNumber(phoneNumber: String?): Long {
    if (phoneNumber == null || !Permissions.checkPermission(
        context,
        Permissions.READ_CONTACTS
      )
    ) {
      return 0
    }
    var phoneContactID = 0L
    try {
      val contact = Uri.encode(phoneNumber)
      val cursor = context.contentResolver.query(
        /* uri = */ Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contact),
        /* projection = */ arrayOf(ContactsContract.PhoneLookup._ID),
        /* selection = */ null,
        /* selectionArgs = */ null,
        /* sortOrder = */ null
      )
      cursor?.use {
        if (it.moveToFirst()) {
          phoneContactID = cursor.getLong(
            cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)
          )
        }
      }
    } catch (e: Throwable) {
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
    val contactLookup = contentResolver.query(
      /* uri = */ uri,
      /* projection = */ arrayOf(ContactsContract.CommonDataKinds.Email.CONTACT_ID),
      /* selection = */ null,
      /* selectionArgs = */ null,
      /* sortOrder = */ null
    )
    contactLookup.use { look ->
      if (look != null && look.count > 0) {
        look.moveToNext()
        contactId = look.readLong(ContactsContract.CommonDataKinds.Email.CONTACT_ID) ?: 0
      }
    }
    return contactId
  }

  fun getNameFromMail(eMail: String?): String? {
    if (eMail == null || !Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
      return null
    }
    val uri = Uri.withAppendedPath(
      ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI,
      Uri.encode(eMail)
    )
    var name: String? = null
    val contentResolver = context.contentResolver
    val contactLookup = contentResolver.query(
      /* uri = */ uri,
      /* projection = */ arrayOf(ContactsContract.Data.DISPLAY_NAME),
      /* selection = */ null,
      /* selectionArgs = */ null,
      /* sortOrder = */ null
    )
    contactLookup.use { look ->
      if (look != null && look.count > 0) {
        look.moveToNext()
        name = look.readString(ContactsContract.Data.DISPLAY_NAME)
      }
    }
    return name
  }

  fun getNameFromNumber(contactNumber: String?): String? {
    var contactName: String? = null
    if (contactNumber != null && Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
      try {
        val contact = Uri.encode(contactNumber)
        val cursor = context.contentResolver.query(
          /* uri = */ Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            contact
          ),
          /* projection = */ arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
          /* selection = */ null,
          /* selectionArgs = */ null,
          /* sortOrder = */ null
        )
        cursor?.use {
          if (it.moveToFirst()) {
            contactName = cursor.getString(
              cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
            )
          }
        }
      } catch (e: Throwable) {
        return contactName
      }
    }
    return contactName
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
          /* uri = */ ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
          /* projection = */ projection,
          /* selection = */ selection,
          /* selectionArgs = */ null,
          /* sortOrder = */ null
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
