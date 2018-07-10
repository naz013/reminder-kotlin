package com.elementary.tasks.google_tasks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListViewModel
import com.elementary.tasks.databinding.FragmentGoogleListBinding
import com.elementary.tasks.navigation.fragments.BaseFragment
import androidx.lifecycle.ViewModelProviders

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

class TaskListFragment : BaseFragment() {

    private var binding: FragmentGoogleListBinding? = null
    private val adapter = TasksRecyclerAdapter()
    private var viewModel: GoogleTaskListViewModel? = null
    private var mId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mId = arguments!!.getString(ARG_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGoogleListBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEmpty()
        initList()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, GoogleTaskListViewModel.Factory(activity!!.application, mId)).get(GoogleTaskListViewModel::class.java)
        viewModel!!.googleTasks.observe(this, { googleTasks ->
            if (googleTasks != null) {
                showTasks(googleTasks)
            }
        })
    }

    private fun showTasks(googleTasks: List<GoogleTask>?) {
        adapter.setGoogleTasks(googleTasks)
        reloadView()
    }

    private fun initList() {
        adapter.actionsListener = { view, position, googleTask, actions ->
            when (actions) {
                ListActions.EDIT -> if (googleTask != null) editTask(googleTask!!)
                ListActions.SWITCH -> if (googleTask != null) viewModel!!.toggleTask(googleTask!!)
            }
        }
    }

    private fun editTask(googleTask: GoogleTask) {
        startActivity(Intent(activity, TaskActivity::class.java)
                .putExtra(Constants.INTENT_ID, googleTask.taskId)
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT))
    }

    private fun initEmpty() {
        binding!!.emptyItem.visibility = View.VISIBLE
        binding!!.emptyText.setText(R.string.no_google_tasks)
        reloadView()
    }

    private fun reloadView() {
        if (adapter.itemCount > 0) {
            binding!!.recyclerView.visibility = View.VISIBLE
            binding!!.emptyItem.visibility = View.GONE
        } else {
            binding!!.recyclerView.visibility = View.GONE
            binding!!.emptyItem.visibility = View.VISIBLE
        }
    }

    companion object {

        private val ARG_ID = "arg_id"

        fun newInstance(id: String?): TaskListFragment {
            val fragment = TaskListFragment()
            val bundle = Bundle()
            bundle.putString(ARG_ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }
}
