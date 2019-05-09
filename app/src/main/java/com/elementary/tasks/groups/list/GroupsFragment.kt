package com.elementary.tasks.groups.list

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.elementary.tasks.databinding.FragmentGroupsBinding
import com.elementary.tasks.groups.create.CreateGroupActivity
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment

class GroupsFragment : BaseNavigationFragment<FragmentGroupsBinding>() {

    private lateinit var viewModel: GroupsViewModel
    private var mAdapter: GroupsRecyclerAdapter = GroupsRecyclerAdapter()

    override fun layoutRes(): Int = R.layout.fragment_groups

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener { addGroup() }
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
                showGroups(groups.toList())
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
            binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            binding.recyclerView.layoutManager = LinearLayoutManager(context)
        }
        binding.recyclerView.adapter = mAdapter
        ViewUtils.listenScrollableView(binding.recyclerView, { setScroll(it) }) {
            if (it) binding.fab.show()
            else binding.fab.hide()
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
                2 -> askConfirmation(t)
            }
        }, *items)
    }

    private fun askConfirmation(t: ReminderGroup) {
        withContext {
            dialogues.askConfirmation(it, getString(R.string.delete)) { b ->
                if (b) viewModel.deleteGroup(t)
            }
        }
    }

    private fun editGroup(t: ReminderGroup) {
        startActivity(Intent(context, CreateGroupActivity::class.java)
                .putExtra(Constants.INTENT_ID, t.groupUuId))
    }

    override fun getTitle(): String = getString(R.string.groups)

    private fun refreshView() {
        if (mAdapter.itemCount == 0) {
            binding.emptyItem.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyItem.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }
}
