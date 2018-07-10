package com.elementary.tasks.core.views

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Typeface
import android.os.AsyncTask
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

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.AssetsUtil
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.elementary.tasks.databinding.ListItemEmailBinding

import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.HashSet

import androidx.appcompat.widget.AppCompatAutoCompleteTextView

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
    private var mTypeface: Typeface? = null
    private var mData: List<EmailItem> = ArrayList()
    private var adapter: EmailAdapter? = null

    private val mLoadCallback = object : EmailCallback {
        override fun onLoadFinish(list: List<EmailItem>) {
            mData = list
            setAdapter(adapter = EmailAdapter(mData))
        }
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.mContext = context
        mTypeface = AssetsUtil.getDefaultTypeface(getContext())
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                performTypeValue(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
        setOnItemClickListener { adapterView, view, i, l -> setText((adapter!!.getItem(i) as EmailItem).email) }
        LoadAsync(mLoadCallback).execute()
    }

    private fun performTypeValue(s: String) {
        if (adapter != null) adapter!!.filter.filter(s)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mTypeface != null) {
            typeface = mTypeface
        }
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

        override fun getItem(i: Int): Any {
            return items[i]
        }

        override fun getItemId(i: Int): Long {
            return 0
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            var view = view
            if (view == null) {
                view = ListItemEmailBinding.inflate(LayoutInflater.from(mContext), viewGroup, false).root
            }
            val item = items[i]
            val nameView = view.findViewById<RoboTextView>(R.id.nameView)
            val emailView = view.findViewById<RoboTextView>(R.id.emailView)
            nameView.text = item.name
            emailView.text = item.email
            return view
        }

        override fun getFilter(): Filter {
            if (filter == null) {
                filter = ValueFilter()
            }
            return filter
        }

        internal inner class ValueFilter : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val results = Filter.FilterResults()
                if (constraint != null && constraint.length > 0) {
                    val filterList = ArrayList<EmailItem>()
                    for (i in mData.indices) {
                        val reference = WeakReference((mData[i].email + mData[i].name).toLowerCase())
                        if (reference.get().contains(constraint.toString().toLowerCase())) {
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

            override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
                if (adapter != null) {
                    adapter!!.setItems(results.values as List<EmailItem>)
                    adapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    private inner class LoadAsync(private val mCallback: EmailCallback?) : AsyncTask<Void, Void, List<EmailItem>>() {

        override fun doInBackground(vararg voids: Void): List<EmailItem> {
            val list = ArrayList<EmailItem>()
            val emlRecsHS = HashSet<String>()
            val cr = mContext!!.contentResolver
            val PROJECTION = arrayOf(ContactsContract.RawContacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_ID, ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.CommonDataKinds.Photo.CONTACT_ID)
            val order = ("CASE WHEN "
                    + ContactsContract.Contacts.DISPLAY_NAME
                    + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                    + ContactsContract.Contacts.DISPLAY_NAME
                    + ", "
                    + ContactsContract.CommonDataKinds.Email.DATA
                    + " COLLATE NOCASE")
            val filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''"
            val cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order)
            if (cur != null && cur.moveToFirst()) {
                do {
                    val name = cur.getString(1)
                    val emlAddr = cur.getString(3)
                    if (emlRecsHS.add(emlAddr.toLowerCase())) {
                        list.add(EmailItem(name, emlAddr))
                    }
                } while (cur.moveToNext())
                cur.close()
            }
            return list
        }

        override fun onPostExecute(list: List<EmailItem>) {
            super.onPostExecute(list)
            mCallback?.onLoadFinish(list)
        }
    }

    private class EmailItem(val name: String, val email: String)

    internal interface EmailCallback {
        fun onLoadFinish(list: List<EmailItem>)
    }

    companion object {

        private val TAG = "EmailAutoCompleteView"
    }
}
