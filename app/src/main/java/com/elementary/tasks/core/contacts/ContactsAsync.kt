package com.elementary.tasks.core.contacts

import android.content.Context
import android.os.AsyncTask
import android.provider.ContactsContract
import com.elementary.tasks.core.utils.Contacts

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
class ContactsAsync(private val mContext: Context, private val mListener: ((List<ContactItem>) -> Unit)?) : AsyncTask<Void, Void, Void>() {

    private var mList: MutableList<ContactItem> = mutableListOf()

    override fun doInBackground(vararg params: Void): Void? {
        val cursor = mContext.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC")
        mList.clear()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                var hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
                val uri = Contacts.getPhoto(id.toLong())
                var photo: String? = null
                if (uri != null) {
                    photo = uri.toString()
                }
                hasPhone = if (hasPhone.equals("1", ignoreCase = true)) {
                    "true"
                } else {
                    "false"
                }
                if (name != null && java.lang.Boolean.parseBoolean(hasPhone)) {
                    val data = ContactItem(name, photo, id)
                    val pos = getPosition(name)
                    if (pos == -1) {
                        mList.add(data)
                    } else {
                        mList.add(pos, data)
                    }
                }
            }
            cursor.close()
        }
        return null
    }

    private fun getPosition(name: String): Int {
        if (mList.size == 0) {
            return 0
        }
        var position = -1
        for (data in mList) {
            val comp = name.compareTo(data.name)
            if (comp <= 0) {
                position = mList.indexOf(data)
                break
            }
        }
        return position
    }

    override fun onPostExecute(aVoid: Void) {
        super.onPostExecute(aVoid)
        mListener?.invoke(mList)
    }
}
