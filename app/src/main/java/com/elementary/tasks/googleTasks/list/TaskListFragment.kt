package com.elementary.tasks.googleTasks.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.googleTasks.GoogleTaskListViewModel
import com.elementary.tasks.googleTasks.create.TaskActivity
import com.elementary.tasks.googleTasks.create.TasksConstants
import kotlinx.android.synthetic.main.fragment_google_list.*
import kotlinx.android.synthetic.main.view_progress.*

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
class TaskListFragment : Fragment() {

    private val adapter = TasksRecyclerAdapter()
    private lateinit var viewModel: GoogleTaskListViewModel
    private var mId: String = ""
    private var mGoogleTaskListsMap: MutableMap<String, GoogleTaskList> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragment = parentFragment
        if (fragment != null) {
            val callback = fragment as PageCallback?
            callback?.provideGoogleTasksLists {
                mapLists(it)
            }
        }
        if (arguments != null) {
            mId = arguments?.getString(ARG_ID) ?: ""
        }
    }

    private fun mapLists(googleTaskLists: List<GoogleTaskList>) {
        if (googleTaskLists.isNotEmpty()) {
            mGoogleTaskListsMap.clear()
            for (list in googleTaskLists) {
                mGoogleTaskListsMap[list.listId] = list
            }
            adapter.googleTaskListMap = mGoogleTaskListsMap
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_google_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressMessageView.text = getString(R.string.please_wait)
        updateProgress(false)
        initEmpty()
        initList()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this,
                GoogleTaskListViewModel.Factory(activity!!.application, mId)).get(GoogleTaskListViewModel::class.java)
        viewModel.isInProgress.observe(this, Observer {
            if (it != null) {
                updateProgress(it)
            }
        })
        viewModel.result.observe(this, Observer {
            if (it != null) {
                showResult(it)
            }
        })
        viewModel.googleTasks.observe(this, Observer{ googleTasks ->
            if (googleTasks != null) {
                showTasks(googleTasks)
            }
        })
    }

    private fun showResult(commands: Commands) {
        when(commands) {
            Commands.FAILED -> {
                Toast.makeText(context!!, getString(R.string.failed_to_update_task), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProgress(b: Boolean) {
        if (b) {
            progressView.visibility = View.VISIBLE
        } else {
            progressView.visibility = View.GONE
        }
    }

    private fun showTasks(googleTasks: List<GoogleTask>) {
        adapter.googleTaskListMap = mGoogleTaskListsMap
        adapter.submitList(googleTasks)
        reloadView()
    }

    private fun initList() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            viewModel.sync()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter.actionsListener = object : ActionsListener<GoogleTask> {
            override fun onAction(view: View, position: Int, t: GoogleTask?, actions: ListActions) {
                when (actions) {
                    ListActions.EDIT -> if (t != null) editTask(t)
                    ListActions.SWITCH -> if (t != null) viewModel.toggleTask(t)
                }
            }
        }
        recyclerView.adapter = adapter
    }

    private fun editTask(googleTask: GoogleTask) {
        startActivity(Intent(activity, TaskActivity::class.java)
                .putExtra(Constants.INTENT_ID, googleTask.taskId)
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT))
    }

    private fun initEmpty() {
        emptyItem.visibility = View.VISIBLE
        emptyText.setText(R.string.no_google_tasks)
        reloadView()
    }

    private fun reloadView() {
        if (adapter.itemCount > 0) {
            emptyItem.visibility = View.GONE
        } else {
            emptyItem.visibility = View.VISIBLE
        }
    }

    companion object {

        private const val ARG_ID = "arg_id"
        private const val ARG_LIST = "arg_list"

        fun newInstance(id: String, googleTaskList: GoogleTaskList? = null): TaskListFragment {
            val fragment = TaskListFragment()
            val bundle = Bundle()
            bundle.putString(ARG_ID, id)
            if (googleTaskList != null) {
                bundle.putSerializable(ARG_LIST, googleTaskList)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
