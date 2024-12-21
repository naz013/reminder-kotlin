package com.elementary.tasks.reminder.lists.removed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.common.android.dp2px
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.github.naz013.feature.common.android.applyBottomInsets
import com.github.naz013.feature.common.android.visibleGone
import com.elementary.tasks.core.views.recyclerview.SpaceBetweenItemDecoration
import com.elementary.tasks.databinding.FragmentTrashBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.elementary.tasks.reminder.lists.ReminderActionResolver
import com.elementary.tasks.reminder.lists.RemindersAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArchiveFragment : BaseToolbarFragment<FragmentTrashBinding>() {

  private val viewModel by viewModel<ArchiveRemindersViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()

  private val reminderResolver by lazy {
    ReminderActionResolver(
      context = requireContext(),
      dialogues = dialogues,
      reminderBuilderLauncher = reminderBuilderLauncher,
      permissionFlow = permissionFlow,
      toggleAction = { },
      deleteAction = { viewModel.deleteReminder(it) },
      skipAction = { }
    )
  }

  private val remindersAdapter = RemindersAdapter(
    isEditable = false,
    onItemClicked = { reminderResolver.resolveItemClick(it.id, it.state.isRemoved) },
    onMoreClicked = { view, reminder ->
      reminderResolver.resolveItemMore(
        view = view,
        id = reminder.id,
        isRemoved = reminder.state.isRemoved,
        actions = reminder.actions
      )
    }
  )

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
    binding.recyclerView.applyBottomInsets()
    addMenu(R.menu.fragment_reminders_archive, { menuItem ->
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
          R.string.archive_was_emptied,
          Toast.LENGTH_SHORT
        ).show()

        else -> {
        }
      }
    }
  }

  override fun getTitle(): String = getString(R.string.reminders_archive)

  private fun showData(result: List<UiReminderEventsList>) {
    remindersAdapter.submitList(result)
    binding.recyclerView.smoothScrollToPosition(0)
    reloadView(result.size)
    if (result.isEmpty()) {
      invalidateOptionsMenu()
    }
  }

  private fun initList() {
    binding.recyclerView.layoutManager = LinearLayoutManager(context)
    binding.recyclerView.adapter = remindersAdapter
    binding.recyclerView.addItemDecoration(SpaceBetweenItemDecoration(dp2px(8)))
    reloadView(0)
  }

  private fun reloadView(count: Int) {
    binding.emptyItem.visibleGone(count == 0)
  }
}
