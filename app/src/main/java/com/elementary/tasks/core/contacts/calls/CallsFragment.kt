package com.elementary.tasks.core.contacts.calls

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.contacts.NumberCallback
import kotlinx.android.synthetic.main.fragment_calls.*

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
class CallsFragment : Fragment() {

    private var mCallback: NumberCallback? = null

    private var mDataList: List<CallsItem> = mutableListOf()
    private var mAdapter: CallsRecyclerAdapter = CallsRecyclerAdapter()

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
        return inflater.inflate(R.layout.fragment_calls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSearchView()
        initRecyclerView()
        refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener { loadData() }
        loadData()
    }

    private fun loadData() {
        CallsAsync(context!!) {
            this.mDataList = it
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
                val number = mAdapter.getItem(it).number
                val name = mAdapter.getItem(it).name
                if (mCallback != null) {
                    mCallback!!.onContactSelected(number, name)
                }
            }
        }
        contactsList.layoutManager = LinearLayoutManager(context)
        contactsList.adapter = mAdapter
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

        fun newInstance(): CallsFragment {
            return CallsFragment()
        }
    }
}
