package com.elementary.tasks.core.contacts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.*
import kotlinx.android.synthetic.main.activity_contacts_list.*
import kotlinx.coroutines.Job
import timber.log.Timber

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
class ContactsActivity : ThemedActivity() {

    private val adapter: ContactsRecyclerAdapter = ContactsRecyclerAdapter()
    private val fullData: MutableList<ContactItem> = mutableListOf()
    private var mLoader: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate: ")
        setContentView(R.layout.activity_contacts_list)
        loaderView.visibility = View.GONE

        initActionBar()
        initSearchView()
        initRecyclerView()
        loadContacts()
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
        contactsList.layoutManager = LinearLayoutManager(this)
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
                filter(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun filter(query: String) {
        var q = query
        q = q.toLowerCase()
        if (q.isEmpty()) {
            adapter.setData(fullData)
            return
        }
        launchDefault {
            val filtered = getFiltered(fullData.toList(), q)
            withUIContext { adapter.setData(filtered) }
        }
    }

    private fun getFiltered(models: List<ContactItem>, query: String): List<ContactItem> {
        val list = ArrayList<ContactItem>()
        for (model in models) {
            val text = model.name.toLowerCase()
            if (text.contains(query)) {
                list.add(model)
            }
        }
        return list
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
                        val builder = dialogues.getDialog(this@ContactsActivity)
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

    private fun loadContacts() {
        showProgress()
        mLoader?.cancel()
        mLoader = launchDefault {
            val mList = mutableListOf<ContactItem>()
            val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                    null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC")
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
                        val pos = getPosition(name, mList)
                        if (pos == -1) {
                            mList.add(data)
                        } else {
                            mList.add(pos, data)
                        }
                    }
                }
                cursor.close()
            }
            withUIContext {
                hideProgress()
                showContacts(mList)
            }
        }
    }

    private fun hideProgress() {
        loaderView.visibility = View.GONE
        typeIcon.isEnabled = true
    }

    private fun showProgress() {
        loaderView.visibility = View.VISIBLE
        typeIcon.isEnabled = false
    }

    private fun showContacts(list: MutableList<ContactItem>) {
        this.fullData.clear()
        this.fullData.addAll(list)
        adapter.setData(list)
        contactsList.smoothScrollToPosition(0)
        refreshView()
    }

    private fun getPosition(name: String, list: List<ContactItem>): Int {
        if (list.isEmpty()) {
            return 0
        }
        var position = -1
        for (data in list) {
            val comp = name.compareTo(data.name)
            if (comp <= 0) {
                position = list.indexOf(data)
                break
            }
        }
        return position
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

    private fun refreshView() {
        if (adapter.itemCount > 0) {
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
