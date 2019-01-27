package com.elementary.tasks.navigation.settings.additional

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.filter.SearchModifier
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.viewModels.smsTemplates.SmsTemplatesViewModel
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_templates_list.*

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
class TemplatesFragment : BaseSettingsFragment() {

    private val adapter = TemplatesAdapter()
    private lateinit var viewModel: SmsTemplatesViewModel

    private var mSearchView: SearchView? = null
    private var mSearchMenu: MenuItem? = null

    private val searchModifier = object : SearchModifier<SmsTemplate>(null, {
        adapter.data = it
        templatesList.smoothScrollToPosition(0)
        refreshView()
    }) {
        override fun filter(v: SmsTemplate): Boolean {
            return searchValue.isEmpty() || v.title.toLowerCase().contains(searchValue.toLowerCase())
        }
    }

    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            searchModifier.setSearchValue(query)
            mSearchMenu?.collapseActionView()
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            searchModifier.setSearchValue(newText)
            return false
        }
    }

    private val mCloseListener = {
        searchModifier.setSearchValue("")
        true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.templates_menu, menu)
        mSearchMenu = menu?.findItem(R.id.action_search)
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        if (mSearchMenu != null) {
            mSearchView = mSearchMenu?.actionView as SearchView?
        }
        if (mSearchView != null) {
            if (searchManager != null) {
                mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
            }
            mSearchView?.setOnQueryTextListener(queryTextListener)
            mSearchView?.setOnCloseListener(mCloseListener)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun layoutRes(): Int = R.layout.fragment_settings_templates_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener { openCreateScreen() }
        initTemplateList()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(SmsTemplatesViewModel::class.java)
        viewModel.smsTemplates.observe(this, Observer { smsTemplates ->
            if (smsTemplates != null) {
                showTemplates(smsTemplates)
            }
        })
    }

    private fun openCreateScreen() {
        startActivity(Intent(context, TemplateActivity::class.java))
    }

    private fun initTemplateList() {
        if (prefs.isTwoColsEnabled && ViewUtils.isHorizontal(context!!)) {
            templatesList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            templatesList.layoutManager = LinearLayoutManager(context)
        }
        adapter.actionsListener = object : ActionsListener<SmsTemplate> {
            override fun onAction(view: View, position: Int, t: SmsTemplate?, actions: ListActions) {
                when (actions) {
                    ListActions.MORE -> if (t != null) {
                        showMenu(view, t)
                    }
                    ListActions.OPEN -> if (t != null) {
                        openTemplate(t)
                    }
                    else -> {
                    }
                }
            }
        }
        templatesList.adapter = adapter
        ViewUtils.listenScrollableView(templatesList) {
            setScroll(it)
        }
        refreshView()
    }

    private fun showMenu(view: View, smsTemplate: SmsTemplate) {
        val items = arrayOf(getString(R.string.edit), getString(R.string.delete))
        Dialogues.showPopup(view, {
            when (it) {
                0 -> openTemplate(smsTemplate)
                1 -> deleteTemplate(smsTemplate)
            }
        }, *items)
    }

    private fun openTemplate(smsTemplate: SmsTemplate) {
        startActivity(Intent(context, TemplateActivity::class.java)
                .putExtra(Constants.INTENT_ID, smsTemplate.key))
    }

    private fun deleteTemplate(smsTemplate: SmsTemplate) {
        viewModel.deleteSmsTemplate(smsTemplate)
    }

    override fun getTitle(): String = getString(R.string.messages)

    private fun showTemplates(smsTemplates: List<SmsTemplate>) {
        searchModifier.original = smsTemplates
    }

    private fun refreshView() {
        if (adapter.itemCount == 0) {
            emptyItem.visibility = View.VISIBLE
            templatesList.visibility = View.GONE
        } else {
            emptyItem.visibility = View.GONE
            templatesList.visibility = View.VISIBLE
        }
    }
}
