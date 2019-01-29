package com.elementary.tasks.core.contacts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.filter.SearchModifier
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.android.synthetic.main.activity_contacts_list.*
import kotlinx.coroutines.Job

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
class SelectContactActivity : ThemedActivity() {

    private lateinit var viewModel: SelectContactViewModel
    private val adapter: ContactsRecyclerAdapter = ContactsRecyclerAdapter()
    private val searchModifier = object : SearchModifier<ContactItem>(null, {
        adapter.submitList(it)
        contactsList.smoothScrollToPosition(0)
        refreshView(it.size)
    }) {
        override fun filter(v: ContactItem): Boolean {
            return searchValue.isEmpty() || v.name.toLowerCase().contains(searchValue.toLowerCase())
        }
    }
    private var mLoader: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SelectContactViewModel::class.java)
        viewModel.contentResolver = contentResolver
        viewModel.loadContacts()

        setContentView(R.layout.activity_contacts_list)
        loaderView.visibility = View.GONE

        initActionBar()
        initSearchView()
        initRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        viewModel.contacts.observe(this, Observer { contacts ->
            contacts?.let { searchModifier.original = it }
        })
        viewModel.isLoading.observe(this, Observer { isLoading ->
            isLoading?.let {
                if (it) showProgress()
                else hideProgress()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mLoader?.cancel()
    }

    private fun initRecyclerView() {
        adapter.clickListener = { name, number ->
            if (number == "") {
                selectNumber(name)
            } else {
                onContactSelected(number, name)
            }
        }
        if (prefs.isTwoColsEnabled && ViewUtils.isHorizontal(this)) {
            contactsList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            contactsList.layoutManager = LinearLayoutManager(this)
        }
        contactsList.adapter = adapter
        contactsList.isNestedScrollingEnabled = false
        ViewUtils.listenScrollableView(scroller) {
            toolbarView.isSelected = it > 0
        }
    }

    private fun initSearchView() {
        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchModifier.setSearchValue(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun selectNumber(name: String) {
        showProgress()
        mLoader?.cancel()
        mLoader = launchDefault {
            val c = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?",
                    arrayOf(name), null)
            if (c  == null) {
                hideProgress()
                return@launchDefault
            }
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
                    withUIContext {
                        hideProgress()
                        val builder = dialogues.getDialog(this@SelectContactActivity)
                        builder.setItems(numbers) { dialog, which ->
                            dialog.dismiss()
                            var number = numbers[which] as String
                            val index = number.indexOf(":")
                            number = number.substring(index + 2)
                            onContactSelected(number, name)
                        }
                        val alert = builder.create()
                        alert.show()
                    }
                }
            } else if (c.count == 1) {
                if (c.moveToFirst()) {
                    withUIContext {
                        hideProgress()
                        onContactSelected(c.getString(phoneIdx), name)
                    }
                }
            } else if (c.count == 0) {
                withUIContext {
                    hideProgress()
                    onContactSelected("", name)
                }
            }
            withUIContext {
                hideProgress()
            }
            c.close()
        }
    }

    override fun onPause() {
        super.onPause()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(searchField.windowToken, 0)
    }

    private fun initActionBar() {
        backButton.setOnClickListener { onBackPressed() }
    }

    private fun hideProgress() {
        loaderView.visibility = View.GONE
        typeIcon.isEnabled = true
    }

    private fun showProgress() {
        loaderView.visibility = View.VISIBLE
        typeIcon.isEnabled = false
    }

    private fun onContactSelected(number: String, name: String) {
        val intent = Intent()
        if (number != "") {
            intent.putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
        }
        intent.putExtra(Constants.SELECTED_CONTACT_NAME, name)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun refreshView(count: Int) {
        if (count > 0) {
            emptyItem.visibility = View.GONE
            scroller.visibility = View.VISIBLE
        } else {
            scroller.visibility = View.GONE
            emptyItem.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }
}
