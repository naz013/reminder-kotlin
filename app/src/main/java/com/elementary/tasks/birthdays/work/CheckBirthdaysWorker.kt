package com.elementary.tasks.birthdays.work

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.readString
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.withContext

class CheckBirthdaysWorker(
  private val birthdaysDao: BirthdaysDao,
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
        ContactsContract.Contacts.CONTENT_URI, projection, null, null,
        ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
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
        ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
          " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE +
          "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId
      val sortOrder = ContactsContract.Contacts.DISPLAY_NAME
      val contacts = birthdaysDao.getAll()
      val birthdayCur = try {
        cr.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder)
      } catch (e: Exception) {
        null
      }
      if (birthdayCur != null && birthdayCur.count > 0) {
        while (birthdayCur.moveToNext()) {
          i += loadBirthday(birthdayCur, contacts, birthdaysDao)
        }
      }
      birthdayCur?.close()
    }
    cur.close()
  }

  private fun loadBirthday(
    birthdayCur: Cursor,
    contacts: List<Birthday>,
    dao: BirthdaysDao
  ): Int {
    val birthday = birthdayCur.readString(ContactsContract.CommonDataKinds.Event.START_DATE)
    val name = birthdayCur.readString(ContactsContract.PhoneLookup.DISPLAY_NAME)
    if (name.isNullOrEmpty()) return 0
    val id = birthdayCur.getLong(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
    val number = contactsReader.getNumber(name)
    var counter = 0
    val date = birthday?.let { dateTimeManager.findBirthdayDate(it) }
    if (date != null) {
      val birthdayItem = Birthday(
        name = name,
        date = dateTimeManager.formatBirthdayDate(date),
        number = number,
        showedYear = 0,
        contactId = id,
        day = date.dayOfMonth,
        month = date.monthValue - 1
      )
      if (!contacts.contains(birthdayItem)) {
        counter += 1
      }
      birthdayItem.updatedAt = dateTimeManager.getNowGmtDateTime()
      dao.insert(birthdayItem)
    }
    return counter
  }
}
