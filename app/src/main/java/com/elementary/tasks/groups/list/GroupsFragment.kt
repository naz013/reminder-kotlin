package com.elementary.tasks.groups.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.group.UiGroupList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.databinding.FragmentGroupsBinding
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.applyBottomInsetsMargin
import org.koin.androidx.viewmodel.ext.android.viewModel

class GroupsFragment : BaseToolbarFragment<FragmentGroupsBinding>() {

  private val viewModel by viewModel<GroupsViewModel>()
  private var groupsRecyclerAdapter = GroupsRecyclerAdapter()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentGroupsBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerView.applyBottomInsets()
    binding.fab.applyBottomInsetsMargin()
    binding.fab.setOnClickListener { addGroup() }
    initGroupsList()
    initViewModel()

    analyticsEventSender.send(ScreenUsedEvent(Screen.GROUPS))
  }

  private fun addGroup() {
    navigate {
      navigate(R.id.editGroupFragment, null, NavigationAnimations.inDepthNavOptions())
    }
  }

  private fun initViewModel() {
    viewModel.allGroups.nonNullObserve(viewLifecycleOwner) { showGroups(it) }
  }

  private fun showGroups(reminderGroups: List<UiGroupList>) {
    groupsRecyclerAdapter.submitList(reminderGroups)
    refreshView()
  }

  private fun changeColor(uiGroupList: UiGroupList) {
    dialogues.showColorDialog(
      activity = requireActivity(),
      current = uiGroupList.colorPosition,
      title = getString(R.string.color),
      colors = ThemeProvider.colorsForSliderThemed(requireContext())
    ) {
      viewModel.changeGroupColor(uiGroupList.id, it)
    }
  }

  private fun initGroupsList() {
    groupsRecyclerAdapter.actionsListener = object : ActionsListener<UiGroupList> {
      override fun onAction(view: View, position: Int, t: UiGroupList?, actions: ListActions) {
        if (t == null) return
        when (actions) {
          ListActions.MORE -> {
            showMore(view, t)
          }

          ListActions.EDIT -> {
            editGroup(t.id)
          }

          else -> {
          }
        }
      }
    }

    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(
        resources.getInteger(R.integer.num_of_cols),
        StaggeredGridLayoutManager.VERTICAL
      )
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    binding.recyclerView.adapter = groupsRecyclerAdapter
    ViewUtils.listenScrollableView(binding.recyclerView) {
      if (it) {
        binding.fab.show()
      } else {
        binding.fab.hide()
      }
    }

    refreshView()
  }

  private fun showMore(view: View, t: UiGroupList) {
    var items = arrayOf(
      getString(R.string.change_color),
      getString(R.string.edit),
      getString(R.string.delete)
    )
    if (groupsRecyclerAdapter.itemCount == 1) {
      items = arrayOf(getString(R.string.change_color), getString(R.string.edit))
    }
    Dialogues.showPopup(view, { item ->
      when (item) {
        0 -> changeColor(t)
        1 -> editGroup(t.id)
        2 -> askConfirmation(t)
      }
    }, *items)
  }

  private fun askConfirmation(t: UiGroupList) {
    withContext {
      dialogues.askConfirmation(it, getString(R.string.delete)) { b ->
        if (b) viewModel.deleteGroup(t.id)
      }
    }
  }

  private fun editGroup(id: String) {
    val bundle = Bundle().apply {
      putString(IntentKeys.INTENT_ID, id)
    }
    navigate {
      navigate(
        R.id.editGroupFragment,
        bundle,
        NavigationAnimations.inDepthNavOptions()
      )
    }
  }

  override fun getTitle(): String = getString(R.string.groups)

  private fun refreshView() {
    if (groupsRecyclerAdapter.itemCount == 0) {
      binding.emptyItem.visibility = View.VISIBLE
      binding.recyclerView.visibility = View.GONE
    } else {
      binding.emptyItem.visibility = View.GONE
      binding.recyclerView.visibility = View.VISIBLE
    }
  }
}
