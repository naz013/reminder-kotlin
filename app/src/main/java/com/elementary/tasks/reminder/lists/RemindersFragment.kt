package com.elementary.tasks.reminder.lists

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.async.SyncTask
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.viewModels.reminders.ActiveRemindersViewModel
import com.elementary.tasks.core.views.FilterView
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import com.elementary.tasks.reminder.lists.filters.ReminderFilterController
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity
import com.elementary.tasks.reminder.preview.ShoppingPreviewActivity
import kotlinx.android.synthetic.main.fragment_reminders.*
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
class RemindersFragment : BaseNavigationFragment(), SyncTask.SyncListener, FilterCallback<Reminder> {

    private lateinit var viewModel: ActiveRemindersViewModel

    private val mAdapter = RemindersRecyclerAdapter()

    private var mGroupsIds = ArrayList<String>()
    private val filters = ArrayList<FilterView.Filter>()
    private val filterController = ReminderFilterController(this)

    private var mSearchView: SearchView? = null
    private var mSearchMenu: MenuItem? = null

    @Inject
    lateinit var reminderUtils: ReminderUtils

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

    private val filterAllElement: FilterView.FilterElement
        get() = FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true)

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_active_menu, menu)
        mSearchMenu = menu!!.findItem(R.id.action_search)
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
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_refresh -> SyncTask(this, false).execute()
            R.id.action_voice -> if (callback != null) {

            }
            R.id.action_filter -> if (callback!!.isFiltersVisible) {
                callback?.hideFilters()
            } else {
                showRemindersFilter()
            }
            R.id.action_exit -> activity?.finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun layoutRes(): Int = R.layout.fragment_reminders

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        viewModel = ViewModelProviders.of(this).get(ActiveRemindersViewModel::class.java)
        viewModel.events.observe(this, Observer{ reminders ->
            if (reminders != null) {
                showData(reminders)
            }
        })
    }

    private fun showData(result: List<Reminder>) {
        filterController.original = result.toMutableList()
        reloadView()
        refreshFilters()
    }

    private fun refreshFilters() {
        filters.clear()
        addDateFilter(filters)
        if (viewModel.allGroups.value != null) {
            addGroupFilter(viewModel.allGroups.value!!)
        }
        addTypeFilter(filters)
        addStatusFilter(filters)
        if (callback!!.isFiltersVisible) {
            showRemindersFilter()
        }
    }

    private fun showActionDialog(reminder: Reminder, view: View) {
        val items = arrayOf(getString(R.string.open), getString(R.string.edit), getString(R.string.change_group), getString(R.string.move_to_trash))
        dialogues.showPopup(context!!, view, { item ->
            when (item) {
                0 -> previewReminder(reminder.uniqueId, reminder.type)
                1 -> editReminder(reminder.uniqueId)
                2 -> changeGroup(reminder)
                3 -> viewModel.moveToTrash(reminder)
            }
        }, *items)
    }

    private fun editReminder(uuId: Int) {
        startActivity(Intent(context, CreateReminderActivity::class.java).putExtra(Constants.INTENT_ID, uuId))
    }

    private fun switchReminder(reminder: Reminder) {
        viewModel.toggleReminder(reminder)
    }

    private fun initList() {
        mAdapter.actionsListener = object : ActionsListener<Reminder> {
            override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
                when (actions) {
                    ListActions.MORE -> if (t != null) showActionDialog(t, view)
                    ListActions.OPEN -> if (t != null) previewReminder(t.uniqueId, t.type)
                    ListActions.SWITCH -> if (t != null) switchReminder(t)
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.tasks))
            callback?.onFragmentSelect(this)
            callback?.onScrollChanged(recyclerView)
        }
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
            if (recyclerView.visibility == View.GONE)
                recyclerView.visibility = View.VISIBLE
            emptyItem.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            emptyItem.visibility = View.VISIBLE
        }
    }

    private fun showRemindersFilter() {
        callback?.addFilters(filters, true)
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
        for (integer in types) {
            filter.add(FilterView.FilterElement(themeUtil.getReminderIllustration(integer), reminderUtils.getType(integer), integer))
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
                val groupsList = ArrayList<String>()
                for (i in ids) groupsList.add(mGroupsIds[i - 1])
                filterController.setGroupValues(groupsList)
            }
        })
        filter.add(FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true))
        for (i in groups.indices) {
            val item = groups[i]
            filter.add(FilterView.FilterElement(themeUtil.getCategoryIndicator(item.color), item.title, i + 1))
            mGroupsIds.add(item.uuId)
        }
        filters.add(filter)
    }

    private fun changeGroup(reminder: Reminder?) {
        mGroupsIds.clear()
        val arrayAdapter = ArrayAdapter<String>(
                context!!, android.R.layout.select_dialog_item)
        val groups = viewModel.allGroups.value
        if (groups != null) {
            for (item in groups) {
                arrayAdapter.add(item.title)
                mGroupsIds.add(item.uuId)
            }
        }
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.choose_group))
        builder.setAdapter(arrayAdapter) { dialog, which ->
            dialog.dismiss()
            val catId = mGroupsIds[which]
            if (reminder!!.groupUuId.matches(catId.toRegex())) {
                Toast.makeText(context, getString(R.string.same_group), Toast.LENGTH_SHORT).show()
                return@setAdapter
            }
            viewModel.changeGroup(reminder, catId)
        }
        val alert = builder.create()
        alert.show()
    }

    override fun endExecution(b: Boolean) {}

    override fun onChanged(result: List<Reminder>) {
        mAdapter.data = result
        recyclerView.smoothScrollToPosition(0)
        reloadView()
    }
}