package com.elementary.tasks.googleTasks

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.viewModels.googleTasks.GoogleTaskListsViewModel
import com.elementary.tasks.googleTasks.create.TaskActivity
import com.elementary.tasks.googleTasks.create.TaskListActivity
import com.elementary.tasks.googleTasks.create.TasksConstants
import com.elementary.tasks.googleTasks.list.PageCallback
import com.elementary.tasks.googleTasks.list.pager.TaskPagerAdapter
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import kotlinx.android.synthetic.main.fragment_google_tasks.*

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
class GoogleTasksFragment : BaseNavigationFragment(), PageCallback {

    private lateinit var viewModel: GoogleTaskListsViewModel
    private var googleTaskLists = listOf<GoogleTaskList>()
    private var taskPagerAdapter: TaskPagerAdapter? = null
    private var currentPos: Int = 0
        get() {
            return pager.currentItem
        }
    private var listId: String? = null
    private var listener: ((String, GoogleTaskComposed) -> Unit)? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.tasks_menu, menu)
        val googleTaskList = currentTaskList()
        if (googleTaskList != null) {
            menu?.add(Menu.NONE, MENU_ITEM_EDIT, 100, R.string.edit_list)
            if (googleTaskList.def != 1) {
                menu?.add(Menu.NONE, MENU_ITEM_DELETE, 100, R.string.delete_list)
            }
            menu?.add(Menu.NONE, MENU_ITEM_CLEAR, 100, R.string.delete_completed_tasks)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_sync -> {
                viewModel.sync()
                return true
            }
            R.id.action_add_list -> {
                startActivity(Intent(context, TaskListActivity::class.java))
                return true
            }
            MENU_ITEM_EDIT -> {
                editListClick()
                return true
            }
            MENU_ITEM_DELETE -> {
                deleteDialog()
                return true
            }
            MENU_ITEM_CLEAR -> {
                clearList()
                return true
            }
            R.id.action_order -> {
                showDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun editListClick() {
        val googleTaskList = currentTaskList()
        if (googleTaskList != null) {
            startActivity(Intent(context, TaskListActivity::class.java)
                    .putExtra(Constants.INTENT_ID, googleTaskList.listId))
        }
    }

    override fun layoutRes(): Int = R.layout.fragment_google_tasks

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }

    private fun currentTaskList(): GoogleTaskList? {
        return if (currentPos > 0) {
            googleTaskLists[currentPos - 1]
        } else {
            null
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(GoogleTaskListsViewModel::class.java)
        viewModel.googleTaskLists.observe(this, Observer { googleTaskLists ->
            if (googleTaskLists != null) {
                showPages(googleTaskLists.toMutableList())
            }
        })
        viewModel.googleTasks.observe(this, Observer {
            val item = listId
            if (it != null && item != null) {
                val foundItem = it.first
                val foundList = it.second
                if (foundItem == item) {
                    listener?.invoke(foundItem, foundList)
                }
            }
        })
    }

    private fun showPages(googleTaskLists: MutableList<GoogleTaskList>) {
        this.googleTaskLists = googleTaskLists

        val pages = mutableListOf("")
        for (list in googleTaskLists) {
            pages.add(list.listId)
        }

        val pos = prefs.lastGoogleList

        taskPagerAdapter = if (Module.isJellyMR1) {
            TaskPagerAdapter(childFragmentManager, pages)
        } else {
            TaskPagerAdapter(fragmentManager!!, pages)
        }
        pager.adapter = taskPagerAdapter
        pager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i2: Int) {

            }

            override fun onPageSelected(i: Int) {
                refreshCurrent(i)
            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })
        pager.currentItem = if (pos < googleTaskLists.size) pos else 0
        refreshCurrent(pager.currentItem)
    }

    private fun refreshCurrent(position: Int) {
        updateScreenTitle()
        prefs.lastGoogleList = position
        activity?.invalidateOptionsMenu()
        taskPagerAdapter?.getCurrent(position)?.requestData()
    }

    override fun getTitle(): String = updateScreenTitle()

    private fun updateScreenTitle(): String {
        var title = getString(R.string.all)
        val currentList = currentTaskList()
        if (currentList != null) {
            title = currentList.title
        }
        callback?.onTitleChange(title)
        return title
    }

    private fun addNewTask() {
        startActivity(Intent(context, TaskActivity::class.java)
                .putExtra(Constants.INTENT_ID, googleTaskLists[currentPos].listId)
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE))
    }

    private fun showDialog() {
        val items = arrayOf(
                getString(R.string.default_string),
                getString(R.string.by_date_az),
                getString(R.string.by_date_za),
                getString(R.string.active_first),
                getString(R.string.completed_first)
        )
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.order)
        builder.setItems(items) { dialog, which ->
            when (which) {
                0 -> prefs.tasksOrder = Constants.ORDER_DEFAULT
                1 -> prefs.tasksOrder = Constants.ORDER_DATE_A_Z
                2 -> prefs.tasksOrder = Constants.ORDER_DATE_Z_A
                3 -> prefs.tasksOrder = Constants.ORDER_COMPLETED_Z_A
                4 -> prefs.tasksOrder = Constants.ORDER_COMPLETED_A_Z
            }
            dialog.dismiss()
            viewModel.reload()
        }
        val alert = builder.create()
        alert.show()
    }

    private fun deleteDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setMessage(R.string.delete_this_list)
        builder.setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
        builder.setPositiveButton(R.string.yes) { dialog, _ ->
            deleteList()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteList() {
        viewModel.deleteGoogleTaskList(googleTaskLists[currentPos])
    }

    private fun clearList() {
        viewModel.clearList(googleTaskLists[currentPos])
    }

    override fun find(listId: String, listener: ((String, GoogleTaskComposed) -> Unit)?) {
        this.listId = listId
        this.listener = listener
        viewModel.findTasks(listId)
    }

    companion object {

        const val MENU_ITEM_EDIT = 12
        const val MENU_ITEM_DELETE = 13
        const val MENU_ITEM_CLEAR = 14
    }
}
