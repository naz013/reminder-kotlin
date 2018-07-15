package com.elementary.tasks.core.contacts.calls

import android.content.Context
import android.os.AsyncTask
import android.provider.CallLog
import com.elementary.tasks.core.utils.Contacts
import com.elementary.tasks.core.utils.Permissions

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
class CallsAsync(private val mContext: Context, private val mListener: ((List<CallsItem>) -> Unit)?) : AsyncTask<Void, Void, Void>() {

    private val mList: MutableList<CallsItem> = mutableListOf()

    override fun doInBackground(vararg params: Void): Void? {
        if (Permissions.checkPermission(mContext, Permissions.READ_CALLS)) {
            val c = mContext.contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null)
            mList.clear()
            if (c != null) {
                val number = c.getColumnIndex(CallLog.Calls.NUMBER)
                val type = c.getColumnIndex(CallLog.Calls.TYPE)
                val date = c.getColumnIndex(CallLog.Calls.DATE)
                val nameIndex = c.getColumnIndex(CallLog.Calls.CACHED_NAME)
                while (c.moveToNext()) {
                    val phoneNumber = c.getString(number)
                    val callType = c.getString(type)
                    val callDate = c.getString(date)
                    val name = c.getString(nameIndex)
                    val id = Contacts.getIdFromNumber(phoneNumber, mContext)
                    var photo: String? = null
                    if (id != 0) {
                        val uri = Contacts.getPhoto(id.toLong())
                        if (uri != null) {
                            photo = uri.toString()
                        }
                    }

                    val data = CallsItem(name, phoneNumber, photo, java.lang.Long.valueOf(callDate), id, Integer.parseInt(callType))
                    val pos = getPosition(data.date)
                    if (pos == -1) {
                        mList.add(data)
                    } else {
                        mList.add(pos, data)
                    }
                }
                c.close()
            }
        }
        return null
    }

    private fun getPosition(date: Long): Int {
        if (mList.size == 0) {
            return 0
        }
        var position = -1
        for (data in mList) {
            if (date > data.date) {
                position = mList.indexOf(data)
                break
            }
        }
        return position
    }

    override fun onPostExecute(aVoid: Void) {
        super.onPostExecute(aVoid)
        mListener?.invoke(mList.toList())
    }
}
