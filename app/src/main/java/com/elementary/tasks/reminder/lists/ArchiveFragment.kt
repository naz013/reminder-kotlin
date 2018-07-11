package com.elementary.tasks.reminder.lists

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.viewModels.reminders.ArchiveRemindersViewModel
import com.elementary.tasks.core.views.FilterView
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity
import com.elementary.tasks.databinding.FragmentTrashBinding
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import com.elementary.tasks.reminder.lists.filters.ReminderFilterController

import java.util.ArrayList
import java.util.LinkedHashSet
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
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
class ArchiveFragment : BaseNavigationFragment(), FilterCallback<Reminder> {

    private var binding: FragmentTrashBinding? = null
    private var viewModel: ArchiveRemindersViewModel? = null

    private var mAdapter: RemindersRecyclerAdapter? = RemindersRecyclerAdapter()

    private var mGroupsIds: MutableList<String> = ArrayList()
    private val filters = ArrayList<FilterView.Filter>()
    private val filterController = ReminderFilterController(this)

    private var mSearchView: SearchView? = null
    private var mSearchMenu: MenuItem? = null

    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            if (mAdapter != null) filterController.setSearchValue(query)
            if (mSearchMenu != null) {
                mSearchMenu!!.collapseActionView()
            }
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            if (mAdapter != null) filterController.setSearchValue(newText)
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
    private val mEventListener = object : RecyclerListener {
        override fun onItemSwitched(position: Int, view: View) {

        }

        override fun onItemClicked(position: Int, view: View) {
            if (view.id == R.id.button_more) {
                showActionDialog(position, view)
            } else {
                val reminder = mAdapter!!.getItem(position)
                if (reminder != null) editReminder(reminder.uniqueId)
            }
        }

        override fun onItemLongClicked(position: Int, view: View) {}
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.archive_menu, menu)
        mSearchMenu = menu!!.findItem(R.id.action_search)
        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        if (mSearchMenu != null) {
            mSearchView = mSearchMenu!!.actionView as SearchView
        }
        if (mSearchView != null) {
            if (searchManager != null) {
                mSearchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
            }
            mSearchView!!.setOnQueryTextListener(queryTextListener)
            mSearchView!!.setOnCloseListener(mSearchCloseListener)
            mSearchView!!.setOnQueryTextFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    if (!callback!!.isFiltersVisible) {
                        showRemindersFilter()
                    }
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_delete_all -> {
                viewModel!!.deleteAll(mAdapter!!.data)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTrashBinding.inflate(inflater, container, false)
        initList()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ArchiveRemindersViewModel::class.java)
        viewModel!!.events.observe(this, { reminders ->
            if (reminders != null) {
                showData(reminders)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.trash))
            callback!!.onFragmentSelect(this)
            callback!!.setClick(null)
        }
    }

    private fun editReminder(id: Int) {
        startActivity(Intent(context, CreateReminderActivity::class.java).putExtra(Constants.INTENT_ID, id))
    }

    private fun showData(result: MutableList<Reminder>?) {
        filterController.original = result!!
        reloadView()
        refreshFilters()
    }

    private fun showActionDialog(position: Int, view: View) {
        val items = arrayOf(getString(R.string.edit), getString(R.string.delete))
        Dialogues.showPopup(context!!, view, { item ->
            val item1 = mAdapter!!.getItem(position)
            if (item1 == null) return@Dialogues.showPopup
            if (item == 0) {
                editReminder(item1!!.uniqueId)
            }
            if (item == 1) {
                viewModel!!.deleteReminder(item1!!, true)
            }
        }, *items)
    }

    private fun refreshFilters() {
        filters.clear()
        if (viewModel!!.groups.value != null) {
            addGroupFilter(viewModel!!.groups.value!!)
            addTypeFilter(filters)
        }
        if (callback!!.isFiltersVisible) {
            showRemindersFilter()
        }
    }

    private fun initList() {
        binding!!.recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = RemindersRecyclerAdapter()
        mAdapter!!.setEventListener(mEventListener)
        mAdapter!!.setEditable(false)
        binding!!.recyclerView.adapter = mAdapter
    }

    private fun showRemindersFilter() {
        callback!!.addFilters(filters, true)
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
            override fun onClick(view: View, id: Int) {
                filterController.setTypeValue(id)
            }

            override fun onMultipleSelected(view: View, ids: List<Int>) {

            }
        })
        filter.add(FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true))
        val util = ThemeUtil.getInstance(context)
        for (integer in types) {
            filter.add(FilterView.FilterElement(util.getReminderIllustration(integer), ReminderUtils.getType(context, integer), integer))
        }
        if (filter.size != 0) {
            filters.add(filter)
        }
    }

    private fun addGroupFilter(groups: List<Group>) {
        mGroupsIds = ArrayList()
        val filter = FilterView.Filter(object : FilterView.FilterElementClick {
            override fun onClick(view: View, id: Int) {
                if (id == 0) {
                    filterController.setGroupValue(null)
                } else {
                    filterController.setGroupValue(mGroupsIds[id - 1])
                }
            }

            override fun onMultipleSelected(view: View, ids: List<Int>) {
                val groups = ArrayList<String>()
                for (i in ids) groups.add(mGroupsIds[i - 1])
                filterController.setGroupValues(groups)
            }
        })
        filter.add(FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true))
        val util = ThemeUtil.getInstance(context)
        for (i in groups.indices) {
            val item = groups[i]
            filter.add(FilterView.FilterElement(util.getCategoryIndicator(item.color), item.title, i + 1))
            mGroupsIds.add(item.uuId)
        }
        filters.add(filter)
    }

    private fun reloadView() {
        if (mAdapter != null && mAdapter!!.itemCount > 0) {
            binding!!.recyclerView.visibility = View.VISIBLE
            binding!!.emptyItem.visibility = View.GONE
        } else {
            binding!!.recyclerView.visibility = View.GONE
            binding!!.emptyItem.visibility = View.VISIBLE
        }
    }

    override fun onChanged(result: List<Reminder>) {
        mAdapter!!.data = result
        binding!!.recyclerView.smoothScrollToPosition(0)
        reloadView()
    }
}
