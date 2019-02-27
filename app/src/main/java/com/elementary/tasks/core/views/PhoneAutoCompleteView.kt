package com.elementary.tasks.core.views

import android.content.Context
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.ListItemEmailBinding
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
class PhoneAutoCompleteView : AppCompatAutoCompleteTextView {

    private var mData: List<PhoneItem> = ArrayList()
    private var adapter: PhoneAdapter? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                performTypeValue(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
        adapter = PhoneAdapter(listOf())
        setAdapter(adapter)
        reloadContacts()
    }

    fun reloadContacts() {
        if (Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
            loadContacts {
                mData = it
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun performTypeValue(s: String) {
        adapter?.filter?.filter(s)
    }

    private inner class PhoneAdapter(items: List<PhoneItem>) : BaseAdapter(), Filterable {

        private var items: List<PhoneItem> = ArrayList()
        private var filter: ValueFilter? = null

        init {
            this.items = items
            getFilter()
        }

        fun setItems(items: List<PhoneItem>) {
            this.items = items
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun getItem(i: Int): String {
            return items[i].phone
        }

        override fun getItemId(i: Int): Long {
            return 0
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View? {
            val newView: View?
            val item = items[i]
            if (view == null) {
                val v = ListItemEmailBinding.inflate(LayoutInflater.from(context), viewGroup, false)
                v.nameView.text = item.name
                v.emailView.text = item.phone
                val h = ViewHolder()
                h.binding = v
                newView = v.root
                newView.tag = h
            } else {
                val h = view.tag as ViewHolder
                h.binding?.let {
                    it.nameView.text = item.name
                    it.emailView.text = item.phone
                }
                newView = h.binding?.root
            }
            return newView
        }

        override fun getFilter(): Filter? {
            if (filter == null) {
                filter = ValueFilter()
            }
            return filter
        }

        inner class ValueFilter : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val matcher = constraint?.toString()?.trim()?.toLowerCase() ?: ""
                val results = Filter.FilterResults()
                if (matcher.isNotEmpty()) {
                    val filterList = mData.filter { it.name.toLowerCase().contains(matcher) || it.phone.contains(matcher) }
                    results.count = filterList.size
                    results.values = filterList
                } else {
                    results.count = mData.size
                    results.values = mData
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
                if (results != null) {
                    adapter?.setItems(results.values as List<PhoneItem>)
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun loadContacts(callback: ((List<PhoneItem>) -> Unit)?) {
        launchDefault {
            val list = ArrayList<PhoneItem>()
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER
            )

            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER))
                    if (number != null && name != null && hasPhone != null && hasPhone == "1") {
                        list.add(PhoneItem(name, number))
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
            withUIContext {
                callback?.invoke(list)
            }
        }
    }

    internal class ViewHolder {
        var binding: ListItemEmailBinding? = null
    }

    data class PhoneItem(val name: String, val phone: String)
}
