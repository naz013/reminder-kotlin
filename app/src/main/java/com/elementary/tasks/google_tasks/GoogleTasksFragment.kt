package com.elementary.tasks.google_tasks

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
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListsViewModel
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.google_tasks.create.TaskListActivity
import com.elementary.tasks.google_tasks.create.TasksConstants
import com.elementary.tasks.google_tasks.list.PageCallback
import com.elementary.tasks.google_tasks.list.pager.TaskPagerAdapter
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import kotlinx.android.synthetic.main.fragment_google_tasks.*
import timber.log.Timber

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
    private var defaultGoogleTaskList: GoogleTaskList? = null
    private val mListenersList: MutableList<(List<GoogleTaskList>) -> Unit> = mutableListOf()
    private var currentPos: Int = 0
        get() {
            return pager.currentItem
        }

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
        fab.setOnClickListener { addNewTask() }
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
        viewModel.defaultTaskList.observe(this, Observer {
            this.defaultGoogleTaskList = it
            refreshFab()
        })
    }

    private val mPageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(i: Int, v: Float, i2: Int) {

        }

        override fun onPageSelected(i: Int) {
            refreshCurrent(i)
        }

        override fun onPageScrollStateChanged(i: Int) {

        }
    }

    private fun showPages(googleTaskLists: MutableList<GoogleTaskList>) {
        this.googleTaskLists = googleTaskLists

        val pages = mutableListOf("")
        for (list in googleTaskLists) {
            pages.add(list.listId)
        }

        val pos = prefs.lastGoogleList

        taskPagerAdapter = TaskPagerAdapter(childFragmentManager, pages)
        pager.offscreenPageLimit = 5
        pager.adapter = taskPagerAdapter
        pager.addOnPageChangeListener(mPageChangeListener)
        pager.currentItem = if (pos < googleTaskLists.size) pos else 0

        notifyFragments(googleTaskLists)

        refreshCurrent(pager.currentItem)
    }

    override fun onBackStackResume() {
        super.onBackStackResume()
        pager.addOnPageChangeListener(mPageChangeListener)
    }

    override fun onPause() {
        super.onPause()
        pager.removeOnPageChangeListener(mPageChangeListener)
    }

    private fun notifyFragments(googleTaskLists: MutableList<GoogleTaskList>) {
        for (listener in mListenersList) {
            listener.invoke(googleTaskLists)
        }
    }

    private fun refreshCurrent(position: Int) {
        Timber.d("refreshCurrent: $position")
        updateScreenTitle()
        prefs.lastGoogleList = position
        activity?.invalidateOptionsMenu()
        refreshFab()
    }

    private fun refreshFab() {
        if (pager.currentItem > 0) {
            fab.show()
        } else {
            if (defaultGoogleTaskList == null) {
                fab.hide()
            } else {
                fab.show()
            }
        }
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
        val currentList = currentTaskList() ?: defaultGoogleTaskList ?: return
        TaskActivity.openLogged(context!!, Intent(context, TaskActivity::class.java)
                .putExtra(Constants.INTENT_ID, currentList.listId)
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE))
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

    override fun provideGoogleTasksLists(listener: ((List<GoogleTaskList>) -> Unit)?) {
        if (listener != null) {
            mListenersList.add(listener)
            listener.invoke(googleTaskLists)
        }
    }

    companion object {

        const val MENU_ITEM_EDIT = 12
        const val MENU_ITEM_DELETE = 13
        const val MENU_ITEM_CLEAR = 14
    }
}
