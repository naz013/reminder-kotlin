package com.elementary.tasks.birthdays.work

import android.content.Context
import android.provider.ContactsContract
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.*
import kotlinx.coroutines.Job
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object ScanContactsWorker {

  private val birthdayFormats = arrayOf<DateFormat>(
    SimpleDateFormat("yyyy-MM-dd", Locale.US),
    SimpleDateFormat("yyyyMMdd", Locale.US),
    SimpleDateFormat("yyyy.MM.dd", Locale.US),
    SimpleDateFormat("yy.MM.dd", Locale.US),
    SimpleDateFormat("MMM dd, yyyy", Locale.US),
    SimpleDateFormat("yy/MM/dd", Locale.US))

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

  fun scan(context: Context) {
    mJob?.cancel()
    launchSync(context)
  }

  private fun launchSync(context: Context) {
    if (!Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
      onEnd?.invoke(0)
      return
    }
    mJob = launchIo {
      val cr = context.contentResolver
      var i = 0
      val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
      val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
        ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC")
      if (cur == null) {
        withUIContext { onEnd?.invoke(0) }
        return@launchIo
      }
      while (cur.moveToNext()) {
        val contactId = cur.getString(cur.getColumnIndex(ContactsContract.Data._ID))
        val columns = arrayOf(ContactsContract.CommonDataKinds.Event.START_DATE, ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.MIMETYPE, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.Contacts._ID)
        val where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
          " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE +
          "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId
        val sortOrder = ContactsContract.Contacts.DISPLAY_NAME
        val dao = AppDb.getAppDatabase(context).birthdaysDao()
        val contacts = dao.all()
        val birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder)
        if (birthdayCur != null && birthdayCur.count > 0) {
          while (birthdayCur.moveToNext()) {
            val birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE))
            val name = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
            val id = birthdayCur.getLong(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val number = Contacts.getNumber(name, context)
            val calendar = Calendar.getInstance()
            for (f in birthdayFormats) {
              var date: Date? = null
              try {
                date = f.parse(birthday)
              } catch (e: Exception) {
                e.printStackTrace()
              }

              if (date != null) {
                calendar.time = date
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)
                val birthdayItem = Birthday(name, TimeUtil.BIRTH_DATE_FORMAT.format(calendar.time), number, 0, id, day, month)
                if (!contacts.contains(birthdayItem)) {
                  i += 1
                }
                birthdayItem.updatedAt = TimeUtil.gmtDateTime
                dao.insert(birthdayItem)
                break
              }
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