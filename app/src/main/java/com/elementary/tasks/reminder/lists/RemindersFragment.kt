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
import android.widget.ArrayAdapter
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.async.SyncTask
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.viewModels.reminders.ActiveRemindersViewModel
import com.elementary.tasks.core.views.FilterView
import com.elementary.tasks.databinding.FragmentRemindersBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.ReminderUpdateEvent
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import com.elementary.tasks.reminder.lists.filters.ReminderFilterController
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity
import com.elementary.tasks.reminder.preview.ShoppingPreviewActivity

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

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
class RemindersFragment : BaseNavigationFragment(), SyncTask.SyncListener, FilterCallback<Reminder> {

    private var binding: FragmentRemindersBinding? = null
    private var viewModel: ActiveRemindersViewModel? = null

    private val mAdapter = RemindersRecyclerAdapter()

    private var mGroupsIds = ArrayList<String>()
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
            switchReminder(position)
        }

        override fun onItemClicked(position: Int, view: View) {
            if (view.id == R.id.button_more) {
                showActionDialog(position, view)
            } else {
                val reminder = mAdapter.getItem(position)
                if (reminder != null) {
                    previewReminder(reminder.uniqueId, reminder.type)
                }
            }
        }

        override fun onItemLongClicked(position: Int, view: View) {}
    }

    private val filterAllElement: FilterView.FilterElement
        get() = FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_active_menu, menu)
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
            R.id.action_refresh -> SyncTask(context, this, false).execute()
            R.id.action_voice -> if (callback != null) {
                callback!!.onVoiceAction()
            }
            R.id.action_filter -> if (callback!!.isFiltersVisible) {
                callback!!.hideFilters()
            } else {
                showRemindersFilter()
            }
            R.id.action_exit -> activity!!.finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRemindersBinding.inflate(inflater, container, false)
        initList()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ActiveRemindersViewModel::class.java)
        viewModel!!.events.observe(this, { reminders ->
            if (reminders != null) {
                showData(reminders)
            }
        })
    }

    private fun showData(result: MutableList<Reminder>?) {
        filterController.original = result!!
        reloadView()
        refreshFilters()
    }

    private fun refreshFilters() {
        filters.clear()
        addDateFilter(filters)
        if (viewModel!!.allGroups.value != null) {
            addGroupFilter(viewModel!!.allGroups.value!!)
        }
        addTypeFilter(filters)
        addStatusFilter(filters)
        if (callback!!.isFiltersVisible) {
            showRemindersFilter()
        }
    }

    private fun showActionDialog(position: Int, view: View) {
        val item1 = mAdapter.getItem(position) ?: return
        val items = arrayOf(getString(R.string.open), getString(R.string.edit), getString(R.string.change_group), getString(R.string.move_to_trash))
        Dialogues.showPopup(context!!, view, { item ->
            when (item) {
                0 -> previewReminder(item1.uniqueId, item1.type)
                1 -> editReminder(item1.uniqueId)
                2 -> changeGroup(item1)
                3 -> viewModel!!.moveToTrash(item1)
            }
        }, *items)
    }

    private fun editReminder(uuId: Int) {
        startActivity(Intent(context, CreateReminderActivity::class.java).putExtra(Constants.INTENT_ID, uuId))
    }

    private fun switchReminder(position: Int) {
        val reminder = mAdapter.getItem(position) ?: return
        viewModel!!.toggleReminder(reminder)
    }

    private fun initList() {
        binding!!.recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.setEventListener(mEventListener)
        binding!!.recyclerView.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.tasks))
            callback!!.onFragmentSelect(this)
            callback!!.setClick { view -> startActivity(Intent(context, CreateReminderActivity::class.java)) }
            callback!!.onScrollChanged(binding!!.recyclerView)
        }
    }

    @Subscribe
    fun onEvent(e: ReminderUpdateEvent) {

    }

    private fun previewReminder(id: Int, type: Int) {
        if (Reminder.isSame(type, Reminder.BY_DATE_SHOP)) {
            context!!.startActivity(Intent(context, ShoppingPreviewActivity::class.java)
                    .putExtra(Constants.INTENT_ID, id))
        } else {
            context!!.startActivity(Intent(context, ReminderPreviewActivity::class.java)
                    .putExtra(Constants.INTENT_ID, id))
        }
    }

    private fun reloadView() {
        if (mAdapter.itemCount > 0) {
            if (binding!!.recyclerView.visibility == View.GONE)
                binding!!.recyclerView.visibility = View.VISIBLE
            binding!!.emptyItem.visibility = View.GONE
        } else {
            binding!!.recyclerView.visibility = View.GONE
            binding!!.emptyItem.visibility = View.VISIBLE
        }
    }

    private fun showRemindersFilter() {
        callback!!.addFilters(filters, true)
    }

    private fun addStatusFilter(filters: MutableList<FilterView.Filter>) {
        val reminders = filterController.original
        if (reminders.size == 0) {
            return
        }
        val filter = FilterView.Filter(object : FilterView.FilterElementClick {
            override fun onClick(view: View, id: Int) {
                filterController.setStatusValue(id)
            }

            override fun onMultipleSelected(view: View, ids: List<Int>) {

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
            override fun onClick(view: View, id: Int) {
                filterController.setRangeValue(id)
            }

            override fun onMultipleSelected(view: View, ids: List<Int>) {

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
            override fun onClick(view: View, id: Int) {
                filterController.setTypeValue(id)
            }

            override fun onMultipleSelected(view: View, ids: List<Int>) {

            }
        })
        filter.add(filterAllElement)
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

    private fun changeGroup(reminder: Reminder?) {
        mGroupsIds.clear()
        val arrayAdapter = ArrayAdapter<String>(
                context!!, android.R.layout.select_dialog_item)
        val groups = viewModel!!.allGroups.value
        for (item in groups!!) {
            arrayAdapter.add(item.title)
            mGroupsIds.add(item.uuId)
        }
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.choose_group))
        builder.setAdapter(arrayAdapter) { dialog, which ->
            dialog.dismiss()
            val catId = mGroupsIds[which]
            if (reminder!!.groupUuId!!.matches(catId.toRegex())) {
                Toast.makeText(context, getString(R.string.same_group), Toast.LENGTH_SHORT).show()
                return@builder.setAdapter
            }
            viewModel!!.changeGroup(reminder, catId)
        }
        val alert = builder.create()
        alert.show()
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun endExecution(b: Boolean) {}

    override fun onChanged(result: List<Reminder>) {
        mAdapter.data = result
        binding!!.recyclerView.smoothScrollToPosition(0)
        reloadView()
    }
}