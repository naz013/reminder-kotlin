package com.elementary.tasks.groups.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.interfaces.SimpleListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.viewModels.groups.GroupsViewModel
import com.elementary.tasks.groups.CreateGroupActivity
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import kotlinx.android.synthetic.main.fragment_groups.*

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
class GroupsFragment : BaseNavigationFragment() {

    private lateinit var viewModel: GroupsViewModel
    private var mAdapter: GroupsRecyclerAdapter = GroupsRecyclerAdapter()

    private val mEventListener = object : SimpleListener {
        override fun onItemClicked(position: Int, view: View) {
            startActivity(Intent(context, CreateGroupActivity::class.java)
                    .putExtra(Constants.INTENT_ID, mAdapter.getItem(position).uuId))
        }

        override fun onItemLongClicked(position: Int, view: View) {
            var items = arrayOf(getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete))
            if (mAdapter.itemCount == 1) {
                items = arrayOf(getString(R.string.change_color), getString(R.string.edit))
            }
            dialogues.showLCAM(context!!, { item ->
                when (item) {
                    0 -> changeColor(mAdapter.getItem(position))
                    1 -> startActivity(Intent(context, CreateGroupActivity::class.java)
                            .putExtra(Constants.INTENT_ID, mAdapter.getItem(position).uuId))
                    2 -> viewModel.deleteGroup(mAdapter.getItem(position))
                }
            }, *items)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initGroupsList()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(GroupsViewModel::class.java)
        viewModel.allGroups.observe(this, Observer{ groups ->
            if (groups != null) {
                showGroups(groups)
            }
        })
    }

    private fun showGroups(reminderGroups: List<ReminderGroup>) {
        mAdapter.setData(reminderGroups)
        refreshView()
    }

    private fun changeColor(reminderGroup: ReminderGroup) {
        var items = arrayOf(getString(R.string.red), getString(R.string.purple), getString(R.string.green), getString(R.string.green_light), getString(R.string.blue), getString(R.string.blue_light), getString(R.string.yellow), getString(R.string.orange), getString(R.string.cyan), getString(R.string.pink), getString(R.string.teal), getString(R.string.amber))
        if (Module.isPro) {
            items = arrayOf(getString(R.string.red), getString(R.string.purple), getString(R.string.green), getString(R.string.green_light), getString(R.string.blue), getString(R.string.blue_light), getString(R.string.yellow), getString(R.string.orange), getString(R.string.cyan), getString(R.string.pink), getString(R.string.teal), getString(R.string.amber), getString(R.string.dark_purple), getString(R.string.dark_orange), getString(R.string.lime), getString(R.string.indigo))
        }
        dialogues.showLCAM(context!!, { item -> viewModel.changeGroupColor(reminderGroup, item) }, *items)
    }

    private fun initGroupsList() {
        mAdapter.mEventListener = mEventListener

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
        refreshView()
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback?.onTitleChange(getString(R.string.groups))
            callback?.onFragmentSelect(this)
            callback?.onScrollChanged(recyclerView)
        }
    }

    private fun refreshView() {
        if (mAdapter.itemCount == 0) {
            emptyItem.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyItem.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
