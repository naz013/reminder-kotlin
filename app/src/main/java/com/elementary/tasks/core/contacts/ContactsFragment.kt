package com.elementary.tasks.core.contacts

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager

import com.elementary.tasks.core.file_explorer.FilterCallback
import com.elementary.tasks.core.file_explorer.RecyclerClickListener
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.databinding.FragmentContactsBinding

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
class ContactsFragment : Fragment(), LoadListener {

    private var mContext: Context? = null
    private var mCallback: NumberCallback? = null

    private var mDataList: List<ContactItem>? = null
    private var mAdapter: ContactsRecyclerAdapter? = null
    private var name: String? = ""

    private var binding: FragmentContactsBinding? = null
    private var mRecyclerView: RecyclerView? = null

    private val mClickListener = RecyclerClickListener { position ->
        if (position != -1) {
            name = mAdapter!!.getItem(position)!!.name
            selectNumber()
        }
    }
    private val mFilterCallback = FilterCallback { size ->
        mRecyclerView!!.scrollToPosition(0)
        refreshView(size)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (mContext == null) {
            mContext = context
        }
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
        if (mContext == null) {
            mContext = activity
        }
        if (mCallback == null) {
            try {
                mCallback = activity as NumberCallback?
            } catch (e: ClassCastException) {
                throw ClassCastException()
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        initSearchView()
        initRecyclerView()
        binding!!.refreshLayout.isRefreshing = true
        binding!!.refreshLayout.setOnRefreshListener(OnRefreshListener { this.loadData() })
        loadData()
        return binding!!.root
    }

    private fun loadData() {
        ContactsAsync(mContext, this).execute()
    }

    private fun initRecyclerView() {
        mRecyclerView = binding!!.contactsList
        mRecyclerView!!.layoutManager = LinearLayoutManager(mContext)
        mRecyclerView!!.setHasFixedSize(true)
    }

    private fun initSearchView() {
        binding!!.searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (mAdapter != null) mAdapter!!.filter(s.toString(), mDataList)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun selectNumber() {
        val c = mContext!!.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?",
                arrayOf<String>(name), null) ?: return
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
                val builder = Dialogues.getDialog(mContext!!)
                builder.setItems(numbers) { dialog, which ->
                    dialog.dismiss()
                    var number = numbers[which] as String
                    val index = number.indexOf(":")
                    number = number.substring(index + 2)
                    if (mCallback != null) {
                        mCallback!!.onContactSelected(number, name)
                    }
                }
                val alert = builder.create()
                alert.show()

            }
        } else if (c.count == 1) {
            if (c.moveToFirst()) {
                val number = c.getString(phoneIdx)
                if (mCallback != null) {
                    mCallback!!.onContactSelected(number, name)
                }
            }
        } else if (c.count == 0) {
            if (mCallback != null) {
                mCallback!!.onContactSelected(null, name)
            }
        }
        c.close()
    }

    private fun refreshView(count: Int) {
        if (count > 0) {
            binding!!.emptyItem.visibility = View.GONE
            mRecyclerView!!.visibility = View.VISIBLE
        } else {
            binding!!.emptyItem.visibility = View.VISIBLE
            mRecyclerView!!.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        val imm = mContext!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(binding!!.searchField.windowToken, 0)
    }

    override fun onLoaded(list: List<ContactItem>) {
        this.mDataList = list
        binding!!.refreshLayout.isRefreshing = false
        mAdapter = ContactsRecyclerAdapter(mContext, list, mClickListener, mFilterCallback)
        mRecyclerView!!.adapter = mAdapter
        refreshView(mAdapter!!.itemCount)
    }

    companion object {

        fun newInstance(): ContactsFragment {
            return ContactsFragment()
        }
    }
}// Required empty public constructor
