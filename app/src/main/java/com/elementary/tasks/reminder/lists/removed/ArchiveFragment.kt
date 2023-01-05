package com.elementary.tasks.reminder.lists.removed

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
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.databinding.FragmentTrashBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.lists.adapter.UiReminderListRecyclerAdapter
import com.elementary.tasks.reminder.lists.filters.SearchModifier
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArchiveFragment : BaseNavigationFragment<FragmentTrashBinding>(),
    (List<UiReminderList>) -> Unit {

  private val viewModel by viewModel<ArchiveRemindersViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()

  private val reminderResolver = ReminderResolver(
    dialogAction = { return@ReminderResolver dialogues },
    toggleAction = { },
    deleteAction = { reminder -> viewModel.deleteReminder(reminder) },
    skipAction = { }
  )

  private var remindersAdapter = UiReminderListRecyclerAdapter(isDark, isEditable = false)
  private val searchModifier = SearchModifier(modifier = null, callback = this)
  private val searchMenuHandler = SearchMenuHandler(systemServiceProvider.provideSearchManager()) {
    searchModifier.setSearchValue(it)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.fragment_trash, menu)
    searchMenuHandler.initSearchMenu(requireActivity(), menu, R.id.action_search)

    val isNotEmpty = (viewModel.events.value?.size ?: 0) > 0
    menu.getItem(0)?.isVisible = isNotEmpty
    menu.getItem(1)?.isVisible = isNotEmpty

    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_delete_all -> {
        viewModel.deleteAll()
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
    viewModel.events.observe(viewLifecycleOwner) { reminders ->
      if (reminders != null) {
        showData(reminders)
      }
    }
    viewModel.result.observe(viewLifecycleOwner) {
      if (it != null) {
        when (it) {
          Commands.DELETED -> Toast.makeText(
            requireContext(),
            R.string.trash_cleared,
            Toast.LENGTH_SHORT
          ).show()

          else -> {
          }
        }
      }
    }
  }

  override fun getTitle(): String = getString(R.string.trash)

  private fun showData(result: List<UiReminderList>) {
    searchModifier.original = result.toMutableList()
    activity?.invalidateOptionsMenu()
  }

  private fun initList() {
    remindersAdapter.actionsListener = object : ActionsListener<UiReminderListData> {
      override fun onAction(
        view: View,
        position: Int,
        t: UiReminderListData?,
        actions: ListActions
      ) {
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
    ViewUtils.listenScrollableView(
      binding.recyclerView,
      { setToolbarAlpha(toAlpha(it.toFloat())) },
      null
    )
    reloadView(0)
  }

  private fun reloadView(count: Int) {
    if (count > 0) {
      binding.emptyItem.visibility = View.GONE
    } else {
      binding.emptyItem.visibility = View.VISIBLE
    }
  }

  override fun invoke(result: List<UiReminderList>) {
    remindersAdapter.submitList(result)
    binding.recyclerView.smoothScrollToPosition(0)
    reloadView(result.size)
  }
}
