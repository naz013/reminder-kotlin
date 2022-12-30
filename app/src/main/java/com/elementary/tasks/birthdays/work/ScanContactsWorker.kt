package com.elementary.tasks.birthdays.work

import android.content.Context
import android.provider.ContactsContract
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.readLong
import com.elementary.tasks.core.utils.io.readString
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.Job

class ScanContactsWorker(
  private val birthdaysDao: BirthdaysDao,
  private val context: Context,
  private val dateTimeManager: DateTimeManager,
  private val contactsReader: ContactsReader
) {

  private var mJob: Job? = null
  var onEnd: ((Int) -> Unit)? = null
  var listener: ((Boolean) -> Unit)? = null
    set(value) {
      field = value
      value?.invoke(mJob != null)
    }

  fun unsubscribe() {
    onEnd = null
    listener = null
  }

  fun scan() {
    mJob?.cancel()
    launchSync()
  }

  private fun launchSync() {
    if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
      onEnd?.invoke(0)
      return
    }
    mJob = launchIo {
      val cr = context.contentResolver
      var i = 0
      val projection =
        arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
      val cur = cr.query(
        ContactsContract.Contacts.CONTENT_URI, projection, null, null,
        ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
      )
      if (cur == null) {
        withUIContext { onEnd?.invoke(0) }
        return@launchIo
      }
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
          ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
            " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE +
            "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId
        val sortOrder = ContactsContract.Contacts.DISPLAY_NAME
        val contacts = birthdaysDao.all()
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
              val birthdayItem = Birthday(
                name,
                dateTimeManager.formatBirthdayDate(date),
                number,
                0,
                id,
                date.dayOfMonth,
                date.monthValue - 1
              )
              if (!contacts.contains(birthdayItem)) {
                i += 1
              }
              birthdayItem.updatedAt = dateTimeManager.getNowGmtDateTime()
              birthdaysDao.insert(birthdayItem)
            }
          }
        }
        birthdayCur?.close()
      }
      cur.close()
      withUIContext { onEnd?.invoke(i) }
      mJob = null
    }
  }
}
