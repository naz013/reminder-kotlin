package com.elementary.tasks.birthdays.work

import android.content.Context
import android.provider.ContactsContract
import android.text.TextUtils
import com.elementary.tasks.birthdays.usecase.SaveBirthdayUseCase
import com.github.naz013.common.Permissions
import com.github.naz013.common.contacts.ContactsReader
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.readLong
import com.github.naz013.feature.common.readString
import com.github.naz013.repository.BirthdayRepository

class ScanContactsWorker(
  private val birthdayRepository: BirthdayRepository,
  private val context: Context,
  private val dateTimeManager: DateTimeManager,
  private val contactsReader: ContactsReader,
  private val saveBirthdayUseCase: SaveBirthdayUseCase
) {

  suspend fun scanContacts(): Int {
    if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
      return 0
    }
    val cr = context.contentResolver
    var i = 0
    val projection =
      arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
    val cur = cr.query(
      /* uri = */ ContactsContract.Contacts.CONTENT_URI,
      /* projection = */ projection,
      /* selection = */ null,
      /* selectionArgs = */ null,
      /* sortOrder = */ ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
    ) ?: return 0

    while (cur.moveToNext()) {
      val contactId = cur.readString(ContactsContract.Data._ID) ?: continue
      val columns = arrayOf(
        ContactsContract.CommonDataKinds.Event.START_DATE,
        ContactsContract.CommonDataKinds.Event.TYPE,
        ContactsContract.CommonDataKinds.Event.MIMETYPE,
        ContactsContract.PhoneLookup.DISPLAY_NAME,
        ContactsContract.Contacts._ID
      )
      val where =
        ContactsContract.CommonDataKinds.Event.TYPE + "=" +
          ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
          " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" +
          ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE +
          "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId
      val sortOrder = ContactsContract.Contacts.DISPLAY_NAME
      val contacts = birthdayRepository.getAll()
      val birthdayCur =
        cr.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder)
      if (birthdayCur != null && birthdayCur.count > 0) {
        while (birthdayCur.moveToNext()) {
          val birthday = birthdayCur.readString(ContactsContract.CommonDataKinds.Event.START_DATE)
          val name = birthdayCur.readString(ContactsContract.PhoneLookup.DISPLAY_NAME, "")
          val id = birthdayCur.readLong(ContactsContract.Contacts._ID)
          val number = contactsReader.getNumber(name)
          val date = birthday?.let { dateTimeManager.findBirthdayDate(it) }
          if (id != null && date != null) {
            val key = if (TextUtils.isEmpty(number)) {
              "0"
            } else {
              number.substring(1)
            }
            val birthdayItem = Birthday(
              name = name,
              date = dateTimeManager.formatBirthdayDate(date),
              number = number,
              showedYear = 0,
              contactId = id,
              day = date.dayOfMonth,
              month = date.monthValue - 1,
              key = "$name|$key",
              updatedAt = dateTimeManager.getNowGmtDateTime(),
              syncState = SyncState.WaitingForUpload
            )
            if (contacts.firstOrNull { it.key == birthdayItem.key } == null) {
              i += 1
              saveBirthdayUseCase(birthdayItem)
            }
          }
        }
      }
      birthdayCur?.close()
    }
    cur.close()
    return i
  }
}
