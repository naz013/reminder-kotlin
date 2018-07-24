package com.elementary.tasks.core.contacts

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.Dialogues
import kotlinx.android.synthetic.main.fragment_contacts.*
import javax.inject.Inject

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
class ContactsFragment : Fragment() {

    private var mCallback: NumberCallback? = null

    private val mDataList: MutableList<ContactItem> = mutableListOf()
    private val mAdapter: ContactsRecyclerAdapter = ContactsRecyclerAdapter()
    private var name: String = ""

    @Inject lateinit var dialogues: Dialogues

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (mCallback == null) {
            try {
                mCallback = context as NumberCallback?
            } catch (e: ClassCastException) {
                throw ClassCastException()
            }

        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (mCallback == null) {
            try {
                mCallback = activity as NumberCallback?
            } catch (e: ClassCastException) {
                throw ClassCastException()
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSearchView()
        initRecyclerView()
        refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener{ this.loadData() }
        loadData()
    }

    private fun loadData() {
        ContactsAsync(context!!) {
            this.mDataList.clear()
            this.mDataList.addAll(it)
            refreshLayout.isRefreshing = false
            mAdapter.setData(it)
            refreshView(mAdapter.itemCount)
        }.execute()
    }

    private fun initRecyclerView() {
        mAdapter.filterCallback = {
            contactsList.scrollToPosition(0)
            refreshView(it)
        }
        mAdapter.clickListener = {
            if (it != -1) {
                name = mAdapter.getItem(it).name
                selectNumber(mAdapter.getItem(it).name)
            }
        }
        contactsList.layoutManager = LinearLayoutManager(context)
        contactsList.setHasFixedSize(true)
    }

    private fun initSearchView() {
        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mAdapter.filter(s.toString(), mDataList)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun selectNumber(name: String) {
        val c = context!!.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?",
                arrayOf(name), null) ?: return
        val phoneIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val phoneType = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
        if (c.count > 1) {
            val numbers = arrayOfNulls<CharSequence>(c.count)
            var i = 0
            if (c.moveToFirst()) {
                while (!c.isAfterLast) {
                    val type = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                            resources, c.getInt(phoneType), "") as String
                    val number = type + ": " + c.getString(phoneIdx)
                    numbers[i++] = number
                    c.moveToNext()
                }
                val builder = dialogues.getDialog(context!!)
                builder.setItems(numbers) { dialog, which ->
                    dialog.dismiss()
                    var number = numbers[which] as String
                    val index = number.indexOf(":")
                    number = number.substring(index + 2)
                    if (mCallback != null) {
                        mCallback!!.onContactSelected(number, this.name)
                    }
                }
                val alert = builder.create()
                alert.show()

            }
        } else if (c.count == 1) {
            if (c.moveToFirst()) {
                val number = c.getString(phoneIdx)
                if (mCallback != null) {
                    mCallback!!.onContactSelected(number, this.name)
                }
            }
        } else if (c.count == 0) {
            if (mCallback != null) {
                mCallback!!.onContactSelected("", this.name)
            }
        }
        c.close()
    }

    private fun refreshView(count: Int) {
        if (count > 0) {
            emptyItem.visibility = View.GONE
            contactsList.visibility = View.VISIBLE
        } else {
            emptyItem.visibility = View.VISIBLE
            contactsList.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(searchField.windowToken, 0)
    }

    companion object {

        fun newInstance(): ContactsFragment {
            return ContactsFragment()
        }
    }
}
