package com.elementary.tasks.core.apps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import kotlinx.android.synthetic.main.activity_application_list.*

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
class ApplicationActivity : ThemedActivity(), FilterCallback<ApplicationItem> {

    private var mAdapter: AppsRecyclerAdapter = AppsRecyclerAdapter()

    private val filterController = AppFilterController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_list)
        initActionBar()
        initSearchView()
        initRecyclerView()
        AppsAsync(this) {
            filterController.original = it
        }.execute()
    }

    private fun initRecyclerView() {
        mAdapter.actionsListener = object : ActionsListener<ApplicationItem> {
            override fun onAction(view: View, position: Int, t: ApplicationItem?, actions: ListActions) {
                if (t != null) {
                    val intent = Intent()
                    intent.putExtra(Constants.SELECTED_APPLICATION, t.packageName)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
        contactsList.layoutManager = LinearLayoutManager(this)
        contactsList.adapter = mAdapter
    }

    private fun initSearchView() {
        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filterController.setSearchValue(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.title = getString(R.string.choose_application)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    }

    override fun onPause() {
        super.onPause()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(searchField.windowToken, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent()
            setResult(RESULT_CANCELED, intent)
            finish()
        }
        return true
    }

    override fun onChanged(result: List<ApplicationItem>) {
        mAdapter.data = result.toMutableList()
        contactsList.smoothScrollToPosition(0)
    }
}
