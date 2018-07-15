package com.elementary.tasks.core.apps

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.fileExplorer.RecyclerClickListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.databinding.ActivityApplicationListBinding
import com.elementary.tasks.reminder.lists.filters.FilterCallback

import androidx.recyclerview.widget.LinearLayoutManager

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
class ApplicationActivity : ThemedActivity(), LoadListener, RecyclerClickListener, FilterCallback<ApplicationItem> {

    private var binding: ActivityApplicationListBinding? = null
    private var mAdapter: AppsRecyclerAdapter? = null

    private val filterController = AppFilterController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_application_list)
        initActionBar()
        initSearchView()
        initRecyclerView()
        AppsAsync(this, this).execute()
    }

    private fun initRecyclerView() {
        binding!!.contactsList.layoutManager = LinearLayoutManager(this)
        binding!!.contactsList.setHasFixedSize(true)
        mAdapter = AppsRecyclerAdapter(this)
        binding!!.contactsList.adapter = mAdapter
    }

    private fun initSearchView() {
        binding!!.searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (mAdapter != null) {
                    filterController.setSearchValue(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        }
        binding!!.toolbar.title = getString(R.string.choose_application)
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    }

    override fun onPause() {
        super.onPause()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(binding!!.searchField.windowToken, 0)
    }

    override fun onLoaded(list: List<ApplicationItem>) {
        filterController.original = list
    }

    override fun onItemClick(position: Int) {
        val intent = Intent()
        val packageName = mAdapter!!.getItem(position).packageName
        intent.putExtra(Constants.SELECTED_APPLICATION, packageName)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent()
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
        return true
    }

    override fun onChanged(result: MutableList<ApplicationItem>) {
        mAdapter!!.data = result
        binding!!.contactsList.smoothScrollToPosition(0)
    }
}
