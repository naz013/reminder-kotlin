package com.elementary.tasks.birthdays.work

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.text.TextUtils
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.contacts.ContactsReader
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.readString
import com.github.naz013.domain.Birthday
import com.github.naz013.repository.BirthdayRepository
import kotlinx.coroutines.withContext

class CheckBirthdaysWorker(
  private val birthdayRepository: BirthdayRepository,
  context: Context,
  workerParams: WorkerParameters,
  private val dateTimeManager: DateTimeManager,
  private val contactsReader: ContactsReader,
  private val dispatcherProvider: DispatcherProvider
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    if (!Permissions.checkPermission(applicationContext, Permissions.READ_CONTACTS)) {
      return Result.success()
    }
    checkDb(applicationContext.contentResolver)
    return Result.success()
  }

  private suspend fun checkDb(cr: ContentResolver) = withContext(dispatcherProvider.default()) {
    var i = 0
    val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
    val cur = try {
      cr.query(
        /* uri = */ ContactsContract.Contacts.CONTENT_URI,
        /* projection = */ projection,
        /* selection = */ null,
        /* selectionArgs = */ null,
        /* sortOrder = */ ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
      )
    } catch (e: Exception) {
      null
    } ?: return@withContext
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
      val birthdayCur = try {
        cr.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder)
      } catch (e: Exception) {
        null
      }
      if (birthdayCur != null && birthdayCur.count > 0) {
        while (birthdayCur.moveToNext()) {
          i += loadBirthday(birthdayCur, contacts)
        }
      }
      birthdayCur?.close()
    }
    cur.close()
  }

  private suspend fun loadBirthday(
    birthdayCur: Cursor,
    contacts: List<Birthday>
  ): Int {
    val birthday = birthdayCur.readString(ContactsContract.CommonDataKinds.Event.START_DATE)
    val name = birthdayCur.readString(ContactsContract.PhoneLookup.DISPLAY_NAME)
    if (name.isNullOrEmpty()) return 0
    val id = birthdayCur.getLong(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
    val number = contactsReader.getNumber(name)
    var counter = 0
    val date = birthday?.let { dateTimeManager.findBirthdayDate(it) }
    val key = if (TextUtils.isEmpty(number)) {
      "0"
    } else {
      number.substring(1)
    }
    if (date != null) {
      val birthdayItem = Birthday(
        name = name,
        date = dateTimeManager.formatBirthdayDate(date),
        number = number,
        showedYear = 0,
        contactId = id,
        day = date.dayOfMonth,
        month = date.monthValue - 1,
        key = "$name|$key",
        updatedAt = dateTimeManager.getNowGmtDateTime()
      )
      if (contacts.firstOrNull { it.key == birthdayItem.key } == null) {
        counter += 1
      }
      birthdayRepository.save(birthdayItem)
    }
    return counter
  }
}
