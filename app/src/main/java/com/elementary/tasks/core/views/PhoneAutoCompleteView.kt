package com.elementary.tasks.core.views

import android.content.Context
import android.graphics.Typeface
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
import com.elementary.tasks.R
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

    private var mTypeface: Typeface? = null
    private var mData: List<PhoneItem> = ArrayList()
    private var adapter: EmailAdapter? = null

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
        reloadContacts()
    }

    fun reloadContacts() {
        if (Permissions.checkPermission(context, Permissions.READ_CONTACTS)) {
            loadContacts {
                mData = it
                setAdapter(EmailAdapter(it))
            }
        }
    }

    private fun performTypeValue(s: String) {
        adapter?.filter?.filter(s)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mTypeface != null) {
            typeface = mTypeface
        }
    }

    private inner class EmailAdapter(items: List<PhoneItem>) : BaseAdapter(), Filterable {

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
            var v = view
            if (v == null) {
                v = LayoutInflater.from(context).inflate(R.layout.list_item_email, viewGroup, false)
            }
            v?.let {
                val item = items[i]
                return ListItemEmailBinding.bind(it).run {
                    nameView.text = item.name
                    emailView.text = item.phone
                    this.root
                }
            }
            return v
        }

        override fun getFilter(): Filter? {
            if (filter == null) {
                filter = ValueFilter()
            }
            return filter
        }

        internal inner class ValueFilter : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val results = Filter.FilterResults()
                if (constraint != null && constraint.isNotEmpty()) {
                    val filterList = ArrayList<PhoneItem>()
                    for (i in mData.indices) {
                        if ((mData[i].phone + " " + mData[i].name).toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filterList.add(mData[i])
                        }
                    }
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

    data class PhoneItem(val name: String, val phone: String)
}
