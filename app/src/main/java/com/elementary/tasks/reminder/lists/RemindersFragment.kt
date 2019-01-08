package com.elementary.tasks.reminder.lists

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.GlobalButtonObservable
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.viewModels.reminders.ActiveRemindersViewModel
import com.elementary.tasks.core.views.FilterView
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.RemindersRecyclerAdapter
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import com.elementary.tasks.reminder.lists.filters.ReminderFilterController
import kotlinx.android.synthetic.main.fragment_reminders.*
import timber.log.Timber
import java.util.*

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
class RemindersFragment : BaseNavigationFragment(), FilterCallback<Reminder> {

    private lateinit var viewModel: ActiveRemindersViewModel

    private val reminderResolver = ReminderResolver(dialogAction = { return@ReminderResolver dialogues},
            saveAction = {reminder -> viewModel.saveReminder(reminder) },
            toggleAction = {reminder -> viewModel.toggleReminder(reminder) },
            deleteAction = {reminder -> viewModel.moveToTrash(reminder) },
            allGroups = { return@ReminderResolver viewModel.groups })

    private val mAdapter = RemindersRecyclerAdapter()

    private var mGroupsIds = ArrayList<String>()
    private val filterController = ReminderFilterController(this)

    private val filterAllElement: FilterView.FilterElement
        get() = FilterView.FilterElement(getString(R.string.all), 0, true)

    private var mSearchView: SearchView? = null
    private var mSearchMenu: MenuItem? = null

    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            filterController.setSearchValue(query)
            if (mSearchMenu != null) {
                mSearchMenu?.collapseActionView()
            }
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            filterController.setSearchValue(newText)
            return false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_active_menu, menu)

        ViewUtils.tintMenuIcon(context!!, menu, 0, R.drawable.ic_twotone_search_24px, isDark)
        ViewUtils.tintMenuIcon(context!!, menu, 1, R.drawable.ic_twotone_mic_24px, isDark)
        ViewUtils.tintMenuIcon(context!!, menu, 2, R.drawable.ic_twotone_filter_list_24px, isDark)

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
        }
        val isNotEmpty = filterController.original.isNotEmpty()
        menu?.getItem(0)?.isVisible = isNotEmpty
        menu?.getItem(2)?.isVisible = false

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_voice -> if (callback != null) {
                buttonObservable.fireAction(view!!, GlobalButtonObservable.Action.VOICE)
            }
            R.id.action_filter -> {
                toggleFilter()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleFilter() {
        if (filterView.visibility == View.GONE) {
            filterView.visibility = View.VISIBLE
        } else {
            filterView.visibility = View.GONE
        }
    }

    override fun layoutRes(): Int = R.layout.fragment_reminders

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener { CreateReminderActivity.openLogged(context!!) }
        fab.setOnLongClickListener {
            buttonObservable.fireAction(it, GlobalButtonObservable.Action.QUICK_NOTE)
            true
        }
        initList()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ActiveRemindersViewModel::class.java)
        viewModel.events.observe(this, Observer{ reminders ->
            if (reminders != null) {
                showData(reminders)
            }
        })
        viewModel.error.observe(this, Observer {
            Timber.d("initViewModel: onError -> $it")
            if (it != null) {
                mAdapter.notifyDataSetChanged()
                Toast.makeText(context!!, it, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showData(result: List<Reminder>) {
        filterController.original = result.toMutableList()
        refreshFilters()
        activity?.invalidateOptionsMenu()
    }

    private fun refreshFilters() {
        filterView.clear()
        addDateFilter()
        addGroupFilter()
        addTypeFilter()
        addStatusFilter()
    }

    private fun initList() {
        mAdapter.actionsListener = object : ActionsListener<Reminder> {
            override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
                if (t != null) {
                    reminderResolver.resolveAction(view, t, actions)
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
        ViewUtils.listenScrollableView(recyclerView) {
            setScroll(it)
        }
        reloadView(0)
    }

    override fun getTitle(): String = getString(R.string.tasks)

    private fun reloadView(count: Int) {
        if (count > 0) {
            emptyItem.visibility = View.GONE
        } else {
            emptyItem.visibility = View.VISIBLE
        }
    }

    private fun addStatusFilter() {
        val reminders = filterController.original
        if (reminders.isEmpty()) {
            return
        }
        val filter = FilterView.Filter(object : FilterView.FilterElementClick {
            override fun onClick(view: View?, id: Int) {
                filterController.setStatusValue(id)
            }
        })
        filter.add(filterAllElement)
        filter.add(FilterView.FilterElement(getString(R.string.enabled4), 1))
        filter.add(FilterView.FilterElement(getString(R.string.disabled), 2))
        filterView.addFilter(filter)
    }

    private fun addDateFilter() {
        val reminders = filterController.original
        if (reminders.isEmpty()) {
            return
        }
        val filter = FilterView.Filter(object : FilterView.FilterElementClick {
            override fun onClick(view: View?, id: Int) {
                filterController.setRangeValue(id)
            }
        })
        filter.add(filterAllElement)
        filter.add(FilterView.FilterElement(getString(R.string.permanent), 1))
        filter.add(FilterView.FilterElement(getString(R.string.today), 2))
        filter.add(FilterView.FilterElement(getString(R.string.tomorrow), 3))
        filterView.addFilter(filter)
    }

    private fun addTypeFilter() {
        val reminders = filterController.original
        if (reminders.isEmpty()) {
            return
        }
        val types = LinkedHashSet<Int>()
        for (reminder in reminders) {
            types.add(reminder.type)
        }
        val filter = FilterView.Filter(object : FilterView.FilterElementClick {
            override fun onClick(view: View?, id: Int) {
                filterController.setTypeValue(id)
            }
        })
        filter.add(filterAllElement)
        for (integer in types) {
            filter.add(FilterView.FilterElement(ReminderUtils.getType(context!!, integer), integer))
        }
        if (filter.isNotEmpty()) {
            filterView.addFilter(filter)
        }
    }

    private fun addGroupFilter() {
        mGroupsIds.clear()
        val reminders = filterController.original
        if (reminders.isEmpty()) {
            return
        }
        val filter = FilterView.Filter(object : FilterView.FilterElementClick {
            override fun onClick(view: View?, id: Int) {
                if (id == 0) {
                    filterController.setGroupValue(null)
                } else {
                    filterController.setGroupValue(mGroupsIds[id - 1])
                }
            }
        })
        val groupIds = mutableMapOf<String, String>()
        reminders.forEach {
            if (it.groupUuId.isNotBlank()) {
                groupIds[it.groupUuId] = it.groupTitle
            }
        }
        filter.add(filterAllElement)
        var count = 1
        for ((key, value) in groupIds.entries) {
            filter.add(FilterView.FilterElement(value, count))
            mGroupsIds.add(key)
            count++
        }
        filterView.addFilter(filter)
    }

    override fun onChanged(result: List<Reminder>) {
        mAdapter.submitList(result)
        recyclerView.smoothScrollToPosition(0)
        reloadView(result.size)
    }

    override fun canGoBack(): Boolean {
        return if (filterView.visibility == View.GONE) {
            true
        } else {
            toggleFilter()
            false
        }
    }
}