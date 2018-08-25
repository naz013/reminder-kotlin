package com.elementary.tasks.google_tasks

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.viewModels.googleTasks.GoogleTaskListsViewModel
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.google_tasks.create.TaskListActivity
import com.elementary.tasks.google_tasks.create.TasksConstants
import com.elementary.tasks.google_tasks.list.TaskPagerAdapter
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import kotlinx.android.synthetic.main.fragment_google_tasks.*
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
class GoogleTasksFragment : BaseNavigationFragment() {

    private lateinit var viewModel: GoogleTaskListsViewModel
    private val googleTaskLists = ArrayList<GoogleTaskList>()
    private var currentPos: Int = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.tasks_menu, menu)
        if (currentPos != 0) {
            menu?.add(Menu.NONE, MENU_ITEM_EDIT, 100, R.string.edit_list)
            val listItem = googleTaskLists[currentPos]
            if (listItem.def != 1) {
                menu?.add(Menu.NONE, MENU_ITEM_DELETE, 100, getStr(R.string.delete_list))
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
                if (currentPos != 0) {
                    startActivity(Intent(context, TaskListActivity::class.java)
                            .putExtra(Constants.INTENT_ID, googleTaskLists[currentPos].listId))
                }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_google_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(GoogleTaskListsViewModel::class.java)
        viewModel.googleTaskLists.observe(this, Observer { googleTaskLists ->
            if (googleTaskLists != null) {
                showPages(googleTaskLists.toMutableList())
            }
        })
    }

    private fun showPages(googleTaskLists: MutableList<GoogleTaskList>) {
        val zeroItem = GoogleTaskList()
        zeroItem.title = getStr(R.string.all)
        zeroItem.color = 25
        googleTaskLists.add(0, zeroItem)
        val pos = prefs.lastGoogleList

        this.googleTaskLists.clear()
        this.googleTaskLists.addAll(googleTaskLists)

        val pagerAdapter: TaskPagerAdapter = if (Module.isJellyMR1) {
            TaskPagerAdapter(childFragmentManager, googleTaskLists)
        } else {
            TaskPagerAdapter(fragmentManager!!, googleTaskLists)
        }
        pager.adapter = pagerAdapter
        pager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i2: Int) {

            }

            override fun onPageSelected(i: Int) {
                updateScreen(i)
                prefs.lastGoogleList = i
                currentPos = i
                activity!!.invalidateOptionsMenu()
            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })
        pager.currentItem = if (pos < googleTaskLists.size) pos else 0
        updateScreen(if (pos < googleTaskLists.size) pos else 0)
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getStr(R.string.google_tasks))
            callback?.onFragmentSelect(this)
        }
    }

    private fun addNewTask() {
        startActivity(Intent(context, TaskActivity::class.java)
                .putExtra(Constants.INTENT_ID, googleTaskLists[currentPos].listId)
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE))
    }

    private fun showDialog() {
        val items = arrayOf(getStr(R.string.default_string), getStr(R.string.by_date_az), getStr(R.string.by_date_za), getStr(R.string.active_first), getStr(R.string.completed_first))
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getStr(R.string.order))
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
        builder.setMessage(getStr(R.string.delete_this_list))
        builder.setNegativeButton(getStr(R.string.no)) { dialog, _ -> dialog.dismiss() }
        builder.setPositiveButton(getStr(R.string.yes)) { dialog, _ ->
            deleteList()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteList() {
        viewModel.deleteGoogleTaskList(googleTaskLists[currentPos])
    }

    private fun updateScreen(pos: Int) {
        if (callback != null) {
            if (pos == 0) {
                callback?.onTitleChange(getStr(R.string.all))
            } else {
                val taskList = googleTaskLists[pos]
                callback?.onTitleChange(taskList.title)
            }
        }
    }


    private fun getStr(@StringRes id: Int): String {
        return if (isAdded) {
            getString(id)
        } else {
            ""
        }
    }

    private fun clearList() {
        viewModel.clearList(googleTaskLists[currentPos])
    }

    companion object {

        const val MENU_ITEM_EDIT = 12
        const val MENU_ITEM_DELETE = 13
        const val MENU_ITEM_CLEAR = 14
    }
}
