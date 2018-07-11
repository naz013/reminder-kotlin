package com.elementary.tasks.birthdays.work

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.provider.ContactsContract
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.Permissions
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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

class CheckBirthdaysAsync : AsyncTask<Void, Void, Int> {

    private lateinit var mContext: Context
    private val birthdayFormats = arrayOf<DateFormat>(SimpleDateFormat("yyyy-MM-dd", Locale.US), SimpleDateFormat("yyyyMMdd", Locale.US), SimpleDateFormat("yyyy.MM.dd", Locale.US), SimpleDateFormat("yy.MM.dd", Locale.US), SimpleDateFormat("MMM dd, yyyy", Locale.US), SimpleDateFormat("yy/MM/dd", Locale.US))

    private var showDialog = false
    private var pd: ProgressDialog? = null
    private var mCallback: (() -> Unit)? = null

    constructor(context: Context) {
        this.mContext = context
    }

    constructor(context: Context, showDialog: Boolean) {
        this.mContext = context
        this.showDialog = showDialog
        if (showDialog) {
            pd = ProgressDialog(context)
            pd!!.setMessage(context.getString(R.string.please_wait))
            pd!!.setCancelable(true)
        }
    }

    constructor(context: Context, showDialog: Boolean, callback: (() -> Unit)?) {
        this.mContext = context
        this.showDialog = showDialog
        this.mCallback = callback
        if (showDialog) {
            pd = ProgressDialog(context)
            pd!!.setMessage(context.getString(R.string.please_wait))
            pd!!.setCancelable(true)
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        if (showDialog) {
            pd!!.show()
        }
    }

    override fun doInBackground(vararg params: Void): Int? {
        if (!Permissions.checkPermission(mContext, Permissions.READ_CONTACTS)) {
            return 0
        }
        val cr = mContext.contentResolver
        var i = 0
        val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC") ?: return 0
        while (cur.moveToNext()) {
            val contactId = cur.getString(cur.getColumnIndex(ContactsContract.Data._ID))
            val columns = arrayOf(ContactsContract.CommonDataKinds.Event.START_DATE, ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.MIMETYPE, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.Contacts._ID)
            val where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                    " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE +
                    "' and " + ContactsContract.Data.CONTACT_ID + " = " + contactId
            val sortOrder = ContactsContract.Contacts.DISPLAY_NAME
            val dao = AppDb.getAppDatabase(mContext).birthdaysDao()
            val contacts = dao.all
            val birthdayCur = cr.query(ContactsContract.Data.CONTENT_URI, columns, where, null, sortOrder)
            if (birthdayCur != null && birthdayCur.count > 0) {
                while (birthdayCur.moveToNext()) {
                    val birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE))
                    val name = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
                    val id = birthdayCur.getInt(birthdayCur.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val number = Contacts.getNumber(name, mContext)
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
                            val birthdayItem = Birthday(name, DATE_FORMAT.format(calendar.time), number, 0, id, day, month)
                            if (!contacts.contains(birthdayItem)) {
                                i = i + 1
                            }
                            dao.insert(birthdayItem)
                            break
                        }
                    }
                }
            }
            birthdayCur?.close()
        }
        cur.close()
        return i
    }

    override fun onPostExecute(files: Int) {
        if (showDialog) {
            try {
                if (pd != null && pd!!.isShowing) {
                    pd!!.dismiss()
                }
            } catch (e: Exception) {
            }

            if (files > 0) {
                Toast.makeText(mContext, files.toString() + " " + mContext.getString(R.string.events_found),
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mContext, R.string.found_nothing,
                        Toast.LENGTH_SHORT).show()
            }
        }
        mCallback?.invoke()
    }

    companion object {
        val DATE_FORMAT: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
}
