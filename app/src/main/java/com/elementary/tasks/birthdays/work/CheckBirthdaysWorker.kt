package com.elementary.tasks.birthdays.work

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.annotation.RequiresPermission
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.dao.BirthdaysDao
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CheckBirthdaysWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val birthdayFormats = arrayOf<DateFormat>(
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("yyyyMMdd", Locale.US),
            SimpleDateFormat("yyyy.MM.dd", Locale.US),
            SimpleDateFormat("yy.MM.dd", Locale.US),
            SimpleDateFormat("MMM dd, yyyy", Locale.US),
            SimpleDateFormat("yy/MM/dd", Locale.US))

    override fun doWork(): Result {
        if (!Permissions.checkPermission(applicationContext, Permissions.READ_CONTACTS)) {
            return Result.success()
        }
        checkDb(applicationContext.contentResolver)
        return Result.success()
    }

    @RequiresPermission(value = Permissions.READ_CONTACTS)
    private fun checkDb(cr: ContentResolver) = launchDefault {
        var i = 0
        val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
        val cur = try {
            cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC")
        } catch (e: Exception) {
            null
        } ?: return@launchDefault
        while (cur.moveToNext()) {
            val contactId = cur.getString(cur.getColumnIndex(ContactsContract.Data._ID))
            val columns = arrayOf(ContactsContract.CommonDataKinds.Event.START_DATE, ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.MIMETYPE, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.Contacts._ID)
            val where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                    " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE +
                    "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId
            val sortOrder = ContactsContract.Contacts.DISPLAY_NAME
            val dao = AppDb.getAppDatabase(applicationContext).birthdaysDao()
            val contacts = dao.all()
            val birthdayCur = try {
                cr.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder)
            } catch (e: Exception) {
                null
            }
            if (birthdayCur != null && birthdayCur.count > 0) {
                while (birthdayCur.moveToNext()) {
                    i += loadBirthday(birthdayCur, contacts, dao)
                }
            }
            birthdayCur?.close()
        }
        cur.close()
    }

    @RequiresPermission(value = Permissions.READ_CONTACTS)
    private suspend fun loadBirthday(
            birthdayCur: Cursor,
            contacts: List<Birthday>,
            dao: BirthdaysDao
    ): Int {
        val birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE))
        val name = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
        if (name.isNullOrEmpty()) return 0
        val id = birthdayCur.getLong(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
        val number = Contacts.getNumber(name, applicationContext)
        val calendar = Calendar.getInstance()
        var counter = 0
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
                    counter += 1
                }
                birthdayItem.updatedAt = TimeUtil.gmtDateTime
                dao.insert(birthdayItem)
                break
            }
        }
        return counter
    }
}
