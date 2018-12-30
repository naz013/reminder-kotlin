package com.elementary.tasks.reminder.lists

import android.app.SearchManager
import android.content.Context
import android.content.Intent
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
import com.elementary.tasks.core.data.models.ReminderGroup
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
    private val filters = ArrayList<FilterView.Filter>()
    private val filterController = ReminderFilterController(this)

    private val filterAllElement: FilterView.FilterElement
        get() = FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true)

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
    private val mSearchCloseListener = {
        refreshFilters()
        false
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
            mSearchView?.setOnCloseListener(mSearchCloseListener)
        }
        val isNotEmpty = viewModel.events.value?.size ?: 0 > 0
        menu?.getItem(0)?.isVisible = isNotEmpty
        menu?.getItem(2)?.isVisible = false

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_voice -> if (callback != null) {
                buttonObservable.fireAction(view!!, GlobalButtonObservable.Action.VOICE)
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun layoutRes(): Int = R.layout.fragment_reminders

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener { startActivity(Intent(activity!!, CreateReminderActivity::class.java)) }
        fab.setOnLongClickListener {
            buttonObservable.fireAction(it, GlobalButtonObservable.Action.QUICK_NOTE)
            true
        }
        reloadView()
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
        filters.clear()
        addDateFilter(filters)
        addGroupFilter(viewModel.groups)
        addTypeFilter(filters)
        addStatusFilter(filters)
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
    }

    override fun getTitle(): String = getString(R.string.tasks)

    private fun reloadView() {
        if (mAdapter.itemCount > 0) {
            emptyItem.visibility = View.GONE
        } else {
            emptyItem.visibility = View.VISIBLE
        }
    }

    private fun showRemindersFilter() {
    }

    private fun addStatusFilter(filters: MutableList<FilterView.Filter>) {
        val reminders = filterController.original
        if (reminders.size == 0) {
            return
        }
        val filter = FilterView.Filter(object : FilterView.FilterElementClick {
            override fun onClick(view: View?, id: Int) {
                filterController.setStatusValue(id)
            }
        })
        filter.add(filterAllElement)
        filter.add(FilterView.FilterElement(R.drawable.ic_power_button, getString(R.string.enabled4), 1))
        filter.add(FilterView.FilterElement(R.drawable.ic_off, getString(R.string.disabled), 2))
        filters.add(filter)
    }

    private fun addDateFilter(filters: MutableList<FilterView.Filter>) {
        val reminders = filterController.original
        if (reminders.size == 0) {
            return
        }
        val filter = FilterView.Filter(object : FilterView.FilterElementClick {
            override fun onClick(view: View?, id: Int) {
                filterController.setRangeValue(id)
            }
        })
        filter.add(filterAllElement)
        filter.add(FilterView.FilterElement(R.drawable.ic_push_pin, getString(R.string.permanent), 1))
        filter.add(FilterView.FilterElement(R.drawable.ic_calendar_illustration, getString(R.string.today), 2))
        filter.add(FilterView.FilterElement(R.drawable.ic_calendar_illustration, getString(R.string.tomorrow), 3))
        filters.add(filter)
    }

    private fun addTypeFilter(filters: MutableList<FilterView.Filter>) {
        val reminders = filterController.original
        if (reminders.size == 0) {
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
            filter.add(FilterView.FilterElement(themeUtil.getReminderIllustration(integer), ReminderUtils.getType(context!!, integer), integer))
        }
        if (filter.size != 0) {
            filters.add(filter)
        }
    }

    private fun addGroupFilter(reminderGroups: List<ReminderGroup>) {
        mGroupsIds = ArrayList()
        val filter = FilterView.Filter(object : FilterView.FilterElementClick {
            override fun onClick(view: View?, id: Int) {
                if (id == 0) {
                    filterController.setGroupValue(null)
                } else {
                    filterController.setGroupValue(mGroupsIds[id - 1])
                }
            }
        })
        filter.add(FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true))
        for (i in reminderGroups.indices) {
            val item = reminderGroups[i]
            filter.add(FilterView.FilterElement(0, item.groupTitle, i + 1))
            mGroupsIds.add(item.groupUuId)
        }
        filters.add(filter)
    }

    override fun onChanged(result: List<Reminder>) {
        Timber.d("onChanged: ${result.size}")
        mAdapter.submitList(result)
        recyclerView.smoothScrollToPosition(0)
        reloadView()
    }
}