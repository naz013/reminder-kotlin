package com.elementary.tasks.core.apps

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityApplicationListBinding

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
class SelectApplicationActivity : ThemedActivity<ActivityApplicationListBinding>() {

    private lateinit var viewModel: SelectApplicationViewModel
    private var adapter: AppsRecyclerAdapter = AppsRecyclerAdapter()
    private val searchModifier = object : SearchModifier<ApplicationItem>(null, {
        adapter.submitList(it)
        binding.contactsList.smoothScrollToPosition(0)
        refreshView(it.size)
    }) {
        override fun filter(v: ApplicationItem): Boolean {
            return searchValue.isEmpty() || (v.name
                    ?: "").toLowerCase().contains(searchValue.toLowerCase())
        }
    }

    override fun layoutRes(): Int = R.layout.activity_application_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SelectApplicationViewModel::class.java)
        viewModel.packageManager = packageManager
        viewModel.loadApps()

        binding.loaderView.visibility = View.GONE
        initActionBar()
        initSearchView()
        initRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        viewModel.applications.observe(this, Observer { applications ->
            applications?.let { searchModifier.original = it }
        })
        viewModel.isLoading.observe(this, Observer { isLoading ->
            isLoading?.let {
                if (it) {
                    showProgress()
                } else {
                    hideProgress()
                }
            }
        })
    }

    private fun hideProgress() {
        binding.loaderView.visibility = View.GONE
    }

    private fun showProgress() {
        binding.loaderView.visibility = View.VISIBLE
    }

    private fun initRecyclerView() {
        adapter.actionsListener = object : ActionsListener<ApplicationItem> {
            override fun onAction(view: View, position: Int, t: ApplicationItem?, actions: ListActions) {
                if (t != null) {
                    val intent = Intent()
                    intent.putExtra(Constants.SELECTED_APPLICATION, t.packageName)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
        if (prefs.isTwoColsEnabled && ViewUtils.isHorizontal(this)) {
            binding.contactsList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            binding.contactsList.layoutManager = LinearLayoutManager(this)
        }
        binding.contactsList.adapter = adapter
        binding.contactsList.isNestedScrollingEnabled = false
        ViewUtils.listenScrollableView(binding.scroller) {
            binding.toolbarView.isSelected = it > 0
        }
    }

    private fun initSearchView() {
        binding.searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchModifier.setSearchValue(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun initActionBar() {
        binding.backButton.setOnClickListener { onBackPressed() }
    }

    override fun onPause() {
        super.onPause()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.searchField.windowToken, 0)
    }

    private fun refreshView(count: Int) {
        if (count > 0) {
            binding.emptyItem.visibility = View.GONE
            binding.scroller.visibility = View.VISIBLE
        } else {
            binding.scroller.visibility = View.GONE
            binding.emptyItem.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }
}
