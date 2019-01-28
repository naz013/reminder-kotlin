package com.elementary.tasks.groups.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.groups.GroupsViewModel
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener { addGroup() }
        initGroupsList()
        initViewModel()
    }

    private fun addGroup() {
        startActivity(Intent(context, CreateGroupActivity::class.java))
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(GroupsViewModel::class.java)
        viewModel.allGroups.observe(this, Observer { groups ->
            if (groups != null) {
                showGroups(groups)
            }
        })
    }

    private fun showGroups(reminderGroups: List<ReminderGroup>) {
        mAdapter.submitList(reminderGroups)
        refreshView()
    }

    private fun changeColor(reminderGroup: ReminderGroup) {
        dialogues.showColorDialog(activity!!, reminderGroup.groupColor, getString(R.string.color), themeUtil.colorsForSlider()) {
            viewModel.changeGroupColor(reminderGroup, it)
        }
    }

    private fun initGroupsList() {
        mAdapter.actionsListener = object : ActionsListener<ReminderGroup> {
            override fun onAction(view: View, position: Int, t: ReminderGroup?, actions: ListActions) {
                if (t == null) return
                when (actions) {
                    ListActions.MORE -> {
                        showMore(view, t)
                    }
                    ListActions.EDIT -> {
                        editGroup(t)
                    }
                    else -> {
                    }
                }
            }
        }

        if (prefs.isTwoColsEnabled && ViewUtils.isHorizontal(context!!)) {
            recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
        recyclerView.adapter = mAdapter
        ViewUtils.listenScrollableView(recyclerView) {
            setScroll(it)
        }

        refreshView()
    }

    private fun showMore(view: View, t: ReminderGroup) {
        var items = arrayOf(getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete))
        if (mAdapter.itemCount == 1) {
            items = arrayOf(getString(R.string.change_color), getString(R.string.edit))
        }
        Dialogues.showPopup(view, { item ->
            when (item) {
                0 -> changeColor(t)
                1 -> editGroup(t)
                2 -> viewModel.deleteGroup(t)
            }
        }, *items)
    }

    private fun editGroup(t: ReminderGroup) {
        startActivity(Intent(context, CreateGroupActivity::class.java)
                .putExtra(Constants.INTENT_ID, t.groupUuId))
    }

    override fun getTitle(): String = getString(R.string.groups)

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
