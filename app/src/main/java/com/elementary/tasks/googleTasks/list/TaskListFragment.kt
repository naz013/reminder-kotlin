package com.elementary.tasks.googleTasks.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.googleTasks.GoogleTaskListViewModel
import com.elementary.tasks.googleTasks.create.TaskActivity
import com.elementary.tasks.googleTasks.create.TasksConstants
import kotlinx.android.synthetic.main.fragment_google_list.*
import kotlinx.coroutines.delay
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

class TaskListFragment : Fragment() {

    private var callback: PageCallback? = null
    private val adapter = TasksRecyclerAdapter()
    private lateinit var viewModel: GoogleTaskListViewModel
    private var mId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragment = parentFragment
        if (fragment != null) {
            callback = fragment as PageCallback?
        }
        if (arguments != null) {
            mId = arguments?.getString(ARG_ID) ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_google_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEmpty()
        initList()
        initViewModel()
    }

    fun requestData() {
        Timber.d("requestData: $mId")
        launchDefault {
            delay(250)
            withUIContext {
                callback?.find(mId) { listId, data ->
                    Timber.d("setModel: $listId, $data")
                    showTasks(data.googleTasks)
                }
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this,
                GoogleTaskListViewModel.Factory(activity!!.application, mId)).get(GoogleTaskListViewModel::class.java)
//        viewModel.googleTasks.observe(this, Observer{ googleTasks ->
//            if (googleTasks != null) {
//                showTasks(googleTasks)
//            }
//        })
    }

    private fun showTasks(googleTasks: List<GoogleTask>) {
        Timber.d("showTasks: $googleTasks")
        adapter.setGoogleTasks(googleTasks)
        reloadView()
    }

    private fun initList() {
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

        fun newInstance(id: String): TaskListFragment {
            val fragment = TaskListFragment()
            val bundle = Bundle()
            bundle.putString(ARG_ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }
}
