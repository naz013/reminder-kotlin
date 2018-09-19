package com.elementary.tasks.reminder.lists

import android.app.SearchManager
import android.content.Context
import android.content.Intent
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
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.viewModels.reminders.ArchiveRemindersViewModel
import com.elementary.tasks.core.views.FilterView
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.RemindersRecyclerAdapter
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import com.elementary.tasks.reminder.lists.filters.ReminderFilterController
import kotlinx.android.synthetic.main.fragment_trash.*
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
class ArchiveFragment : BaseNavigationFragment(), FilterCallback<Reminder> {

    private lateinit var viewModel: ArchiveRemindersViewModel

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
            if (!callback!!.isFiltersVisible) {
                showRemindersFilter()
            }
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
            mSearchView?.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (!callback!!.isFiltersVisible) {
                        showRemindersFilter()
                    }
                }
            }
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
        initList()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ArchiveRemindersViewModel::class.java)
        viewModel.events.observe(this, Observer{ reminders ->
            if (reminders != null) {
                showData(reminders)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.trash))
            callback?.onFragmentSelect(this)
        }
    }

    private fun editReminder(reminder: Reminder) {
        startActivity(Intent(context, CreateReminderActivity::class.java)
                .putExtra(Constants.INTENT_ID, reminder.uuId))
    }

    private fun showData(result: List<Reminder>) {
        filterController.original = result.toMutableList()
        reloadView()
        refreshFilters()
        activity?.invalidateOptionsMenu()
    }

    private fun showActionDialog(reminder: Reminder, view: View) {
        val items = arrayOf(getString(R.string.edit), getString(R.string.delete))
        dialogues.showPopup(context!!, view, { item ->
            if (item == 0) {
                editReminder(reminder)
            }
            if (item == 1) {
                viewModel.deleteReminder(reminder, true)
            }
        }, *items)
    }

    private fun refreshFilters() {
        filters.clear()
        if (viewModel.groups.value != null) {
            addGroupFilter(viewModel.groups.value!!)
            addTypeFilter(filters)
        }
        if (callback!!.isFiltersVisible) {
            showRemindersFilter()
        }
    }

    private fun initList() {
        mAdapter.setEditable(false)
        mAdapter.actionsListener = object : ActionsListener<Reminder> {
            override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
                when (actions) {
                    ListActions.MORE -> if (t != null) showActionDialog(t, view)
                    ListActions.OPEN -> if (t != null) editReminder(t)
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
    }

    private fun showRemindersFilter() {
        callback?.addFilters(filters, true)
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
            recyclerView.visibility = View.VISIBLE
            emptyItem.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            emptyItem.visibility = View.VISIBLE
        }
    }

    override fun onChanged(result: List<Reminder>) {
        mAdapter.data = result
        recyclerView.smoothScrollToPosition(0)
        reloadView()
    }
}
