package com.elementary.tasks.reminder.lists.removed

import android.os.Bundle
import android.view.LayoutInflater
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
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentTrashBinding
import com.elementary.tasks.navigation.fragments.BaseAnimatedFragment
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.lists.adapter.UiReminderListRecyclerAdapter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArchiveFragment : BaseAnimatedFragment<FragmentTrashBinding>() {

  private val viewModel by viewModel<ArchiveRemindersViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()

  private val reminderResolver = ReminderResolver(
    dialogAction = { return@ReminderResolver dialogues },
    toggleAction = { },
    deleteAction = { reminder -> viewModel.deleteReminder(reminder) },
    skipAction = { }
  )

  private var remindersAdapter = UiReminderListRecyclerAdapter(isDark, isEditable = false)
  private val searchMenuHandler = SearchMenuHandler(
    systemServiceProvider.provideSearchManager(),
    R.string.search
  ) { viewModel.onSearchUpdate(it) }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentTrashBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    addMenu(R.menu.fragment_archived_reminders, { menuItem ->
      return@addMenu when (menuItem.itemId) {
        R.id.action_delete_all -> {
          viewModel.deleteAll()
          true
        }

        else -> false
      }
    }) {
      searchMenuHandler.initSearchMenu(requireActivity(), it, R.id.action_search)
      it.getItem(1)?.isVisible = viewModel.hasEvents()
    }
    initList()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.events.nonNullObserve(viewLifecycleOwner) { showData(it) }
    viewModel.result.nonNullObserve(viewLifecycleOwner) {
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

  override fun getTitle(): String = getString(R.string.trash)

  private fun showData(result: List<UiReminderList>) {
    remindersAdapter.submitList(result)
    binding.recyclerView.smoothScrollToPosition(0)
    reloadView(result.size)
    if (result.isEmpty()) {
      activity?.invalidateOptionsMenu()
    }
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
    reloadView(0)
  }

  private fun reloadView(count: Int) {
    binding.emptyItem.visibleGone(count == 0)
  }
}
