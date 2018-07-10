package com.elementary.tasks.groups.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.interfaces.SimpleListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.view_models.groups.GroupsViewModel
import com.elementary.tasks.databinding.FragmentGroupsBinding
import com.elementary.tasks.groups.CreateGroupActivity
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
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
class GroupsFragment : BaseNavigationFragment() {

    private var binding: FragmentGroupsBinding? = null
    private var viewModel: GroupsViewModel? = null
    private var mAdapter: GroupsRecyclerAdapter? = null

    private val mEventListener = object : SimpleListener {
        override fun onItemClicked(position: Int, view: View) {
            startActivity(Intent(context, CreateGroupActivity::class.java).putExtra(Constants.INTENT_ID, mAdapter!!.getItem(position).uuId))
        }

        override fun onItemLongClicked(position: Int, view: View) {
            var items = arrayOf(getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete))
            if (mAdapter!!.itemCount == 1) {
                items = arrayOf(getString(R.string.change_color), getString(R.string.edit))
            }
            Dialogues.showLCAM(context!!, { item ->
                when (item) {
                    0 -> changeColor(mAdapter!!.getItem(position))
                    1 -> startActivity(Intent(context, CreateGroupActivity::class.java)
                            .putExtra(Constants.INTENT_ID, mAdapter!!.getItem(position).uuId))
                    2 -> viewModel!!.deleteGroup(mAdapter!!.getItem(position))
                }
            }, *items)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGroupsBinding.inflate(inflater, container, false)
        initGroupsList()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(GroupsViewModel::class.java)
        viewModel!!.allGroups.observe(this, { groups ->
            if (groups != null) {
                showGroups(groups)
            }
        })
    }

    private fun showGroups(groups: List<Group>?) {
        mAdapter!!.setData(groups)
        refreshView()
    }

    private fun changeColor(group: Group) {
        var items = arrayOf(getString(R.string.red), getString(R.string.purple), getString(R.string.green), getString(R.string.green_light), getString(R.string.blue), getString(R.string.blue_light), getString(R.string.yellow), getString(R.string.orange), getString(R.string.cyan), getString(R.string.pink), getString(R.string.teal), getString(R.string.amber))
        if (Module.isPro) {
            items = arrayOf(getString(R.string.red), getString(R.string.purple), getString(R.string.green), getString(R.string.green_light), getString(R.string.blue), getString(R.string.blue_light), getString(R.string.yellow), getString(R.string.orange), getString(R.string.cyan), getString(R.string.pink), getString(R.string.teal), getString(R.string.amber), getString(R.string.dark_purple), getString(R.string.dark_orange), getString(R.string.lime), getString(R.string.indigo))
        }
        Dialogues.showLCAM(context!!, { item -> viewModel!!.changeGroupColor(group, item) }, *items)
    }

    private fun initGroupsList() {
        binding!!.recyclerView.setHasFixedSize(false)
        binding!!.recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = GroupsRecyclerAdapter(mEventListener)
        binding!!.recyclerView.adapter = mAdapter
        refreshView()
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.groups))
            callback!!.onFragmentSelect(this)
            callback!!.setClick { view -> startActivity(Intent(context, CreateGroupActivity::class.java)) }
            callback!!.onScrollChanged(binding!!.recyclerView)
        }
    }

    private fun refreshView() {
        if (mAdapter == null || mAdapter!!.itemCount == 0) {
            binding!!.emptyItem.visibility = View.VISIBLE
            binding!!.recyclerView.visibility = View.GONE
        } else {
            binding!!.emptyItem.visibility = View.GONE
            binding!!.recyclerView.visibility = View.VISIBLE
        }
    }
}
