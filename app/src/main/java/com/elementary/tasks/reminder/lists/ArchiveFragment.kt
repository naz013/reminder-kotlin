package com.elementary.tasks.reminder.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.SearchMenuHandler
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.reminders.ArchiveRemindersViewModel
import com.elementary.tasks.databinding.FragmentTrashBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.lists.adapter.ReminderAdsViewHolder
import com.elementary.tasks.reminder.lists.adapter.RemindersRecyclerAdapter
import com.elementary.tasks.reminder.lists.filters.SearchModifier
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArchiveFragment : BaseNavigationFragment<FragmentTrashBinding>(), (List<Reminder>) -> Unit {

  private val viewModel by viewModel<ArchiveRemindersViewModel>()

  private val reminderResolver = ReminderResolver(
    dialogAction = { return@ReminderResolver dialogues },
    saveAction = { reminder -> viewModel.saveReminder(reminder) },
    toggleAction = { },
    deleteAction = { reminder -> viewModel.deleteReminder(reminder, true) },
    skipAction = { },
    allGroups = { return@ReminderResolver viewModel.groups }
  )

  private var remindersAdapter = RemindersRecyclerAdapter(currentStateHolder, showHeader = false, isEditable = false) {
    showData(viewModel.events.value ?: listOf())
  }
  private val searchModifier = SearchModifier(modifier = null, callback = this)
  private val searchMenuHandler = SearchMenuHandler { searchModifier.setSearchValue(it) }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.fragment_trash, menu)
    searchMenuHandler.initSearchMenu(requireActivity(), menu, R.id.action_search)

    val isNotEmpty = viewModel.events.value?.size ?: 0 > 0
    menu.getItem(0)?.isVisible = isNotEmpty
    menu.getItem(1)?.isVisible = isNotEmpty

    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_delete_all -> {
        viewModel.deleteAll(remindersAdapter.data)
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentTrashBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initList()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.events.observe(viewLifecycleOwner, { reminders ->
      if (reminders != null) {
        showData(reminders)
      }
    })
    viewModel.result.observe(viewLifecycleOwner, {
      if (it != null) {
        when (it) {
          Commands.DELETED -> Toast.makeText(requireContext(), R.string.trash_cleared, Toast.LENGTH_SHORT).show()
          else -> {
          }
        }
      }
    })
  }

  override fun getTitle(): String = getString(R.string.trash)

  private fun showData(result: List<Reminder>) {
    searchModifier.original = result.toMutableList()
    activity?.invalidateOptionsMenu()
  }

  private fun initList() {
    remindersAdapter.actionsListener = object : ActionsListener<Reminder> {
      override fun onAction(view: View, position: Int, t: Reminder?, actions: ListActions) {
        if (t != null) {
          reminderResolver.resolveAction(view, t, actions)
        }
      }
    }
    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    binding.recyclerView.adapter = remindersAdapter
    ViewUtils.listenScrollableView(binding.recyclerView, { setToolbarAlpha(toAlpha(it.toFloat())) }, null)
    reloadView(0)
  }

  private fun reloadView(count: Int) {
    if (count > 0) {
      binding.emptyItem.visibility = View.GONE
    } else {
      binding.emptyItem.visibility = View.VISIBLE
    }
  }

  override fun invoke(result: List<Reminder>) {
    val newList = ReminderAdsViewHolder.updateList(result)
    remindersAdapter.submitList(newList)
    binding.recyclerView.smoothScrollToPosition(0)
    reloadView(newList.size)
  }

  override fun onDestroy() {
    remindersAdapter.onDestroy()
    super.onDestroy()
  }
}
