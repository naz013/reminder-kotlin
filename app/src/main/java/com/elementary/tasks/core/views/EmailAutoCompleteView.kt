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
class EmailAutoCompleteView : AppCompatAutoCompleteTextView {

    private var mContext: Context? = null
    private var mData: List<EmailItem> = ArrayList()
    private var adapter: EmailAdapter? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        this.mContext = context
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                performTypeValue(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
        adapter = EmailAdapter(listOf())
        setAdapter(adapter)
        loadContacts {
            mData = it
            adapter?.notifyDataSetChanged()
        }
    }

    private fun performTypeValue(s: String) {
        adapter?.filter?.filter(s)
    }

    private inner class EmailAdapter(items: List<EmailItem>) : BaseAdapter(), Filterable {

        private var items: List<EmailItem> = ArrayList()
        private var filter: ValueFilter? = null

        init {
            this.items = items
            getFilter()
        }

        fun setItems(items: List<EmailItem>) {
            this.items = items
        }

        override fun getCount(): Int {
            return items.size
        }

        override fun getItem(i: Int): String {
            return items[i].email
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
                v.emailView.text = item.email
                val h = ViewHolder()
                h.binding = v
                newView = v.root
                newView.tag = h
            } else {
                val h = view.tag as ViewHolder
                h.binding?.let {
                    it.nameView.text = item.name
                    it.emailView.text = item.email
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

        internal inner class ValueFilter : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val matcher = constraint?.toString()?.trim()?.toLowerCase() ?: ""
                val results = Filter.FilterResults()
                if (matcher.isNotEmpty()) {
                    val filterList = mData.filter { it.name.toLowerCase().contains(matcher) || it.email.contains(matcher) }
                    results.count = filterList.size
                    results.values = filterList
                } else {
                    results.count = mData.size
                    results.values = mData
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {
                if (adapter != null && results != null) {
                    adapter?.setItems(results.values as List<EmailItem>)
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun loadContacts(callback: ((List<EmailItem>) -> Unit)?) {
        launchDefault {
            val list = ArrayList<EmailItem>()
            val uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
            val projection = arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Email.DATA
            )

            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val emlAddr = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                    if (emlAddr != null && name != null) {
                        list.add(EmailItem(name, emlAddr))
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

    data class EmailItem(val name: String, val email: String)
}
