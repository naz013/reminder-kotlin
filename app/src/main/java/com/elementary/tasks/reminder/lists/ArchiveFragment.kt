package com.elementary.tasks.reminder.lists

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.viewModels.reminders.ArchiveRemindersViewModel
import com.elementary.tasks.core.views.FilterView
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.lists.adapter.RemindersRecyclerAdapter
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import com.elementary.tasks.reminder.lists.filters.ReminderFilterController
import kotlinx.android.synthetic.main.fragment_trash.*
import java.util.*
import javax.inject.Inject

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
class ArchiveFragment : BaseNavigationFragment(), FilterCallback<Reminder> {

    private lateinit var viewModel: ArchiveRemindersViewModel
    @Inject
    lateinit var reminderUtils: ReminderUtils

    private val reminderResolver = ReminderResolver(dialogAction = { return@ReminderResolver dialogues },
            saveAction = { reminder -> viewModel.saveReminder(reminder) },
            toggleAction = {},
            deleteAction = { reminder -> viewModel.deleteReminder(reminder, true) },
            allGroups = { return@ReminderResolver viewModel.groups })

    private var mAdapter = RemindersRecyclerAdapter()

    private var mGroupsIds: MutableList<String> = ArrayList()
    private val filters = ArrayList<FilterView.Filter>()
    private val filterController = ReminderFilterController(this)

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

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_trash, menu)
        val searchIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_twotone_search_24px)
        val deleteIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_twotone_delete_sweep_24px)
        if (isDark) {
            DrawableCompat.setTint(searchIcon!!, ContextCompat.getColor(context!!, R.color.whitePrimary))
            DrawableCompat.setTint(deleteIcon!!, ContextCompat.getColor(context!!, R.color.whitePrimary))
        } else {
            DrawableCompat.setTint(searchIcon!!, ContextCompat.getColor(context!!, R.color.pureBlack))
            DrawableCompat.setTint(deleteIcon!!, ContextCompat.getColor(context!!, R.color.pureBlack))
        }
        menu?.getItem(0)?.icon = searchIcon
        menu?.getItem(1)?.icon = deleteIcon

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
        menu?.getItem(1)?.isVisible = isNotEmpty

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item == null) return false
        when (item.itemId) {
            R.id.action_delete_all -> {
                viewModel.deleteAll(mAdapter.data)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun layoutRes(): Int = R.layout.fragment_trash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reloadView()
        initList()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ArchiveRemindersViewModel::class.java)
        viewModel.events.observe(this, Observer { reminders ->
            if (reminders != null) {
                showData(reminders)
            }
        })
    }

    override fun getTitle(): String = getString(R.string.trash)

    private fun showData(result: List<Reminder>) {
        filterController.original = result.toMutableList()
        reloadView()
        refreshFilters()
        activity?.invalidateOptionsMenu()
    }

    private fun refreshFilters() {
        filters.clear()
        addGroupFilter(viewModel.groups)
        addTypeFilter(filters)
    }

    private fun initList() {
        mAdapter.setEditable(false)
        mAdapter.showHeader = false
        mAdapter.actionsListener = object : ActionsListener<Reminder> {
            override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
                if (t != null) {
                    reminderResolver.resolveAction(view, t, actions)
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
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
        filter.add(FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true))
        for (integer in types) {
            filter.add(FilterView.FilterElement(themeUtil.getReminderIllustration(integer), reminderUtils.getType(integer), integer))
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
            filter.add(FilterView.FilterElement(themeUtil.getCategoryIndicator(item.groupColor), item.groupTitle, i + 1))
            mGroupsIds.add(item.groupUuId)
        }
        filters.add(filter)
    }

    private fun reloadView() {
        if (mAdapter.itemCount > 0) {
            emptyItem.visibility = View.GONE
        } else {
            emptyItem.visibility = View.VISIBLE
        }
    }

    override fun onChanged(result: List<Reminder>) {
        mAdapter.data = result
        recyclerView.smoothScrollToPosition(0)
        reloadView()
    }
}
