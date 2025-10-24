package com.elementary.tasks.reminder.lists.removed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.views.recyclerview.SpaceBetweenItemDecoration
import com.elementary.tasks.databinding.FragmentTrashBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.elementary.tasks.reminder.lists.ReminderActionResolver
import com.elementary.tasks.reminder.lists.RemindersAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList
import com.elementary.tasks.reminder.lists.filter.Filters
import com.elementary.tasks.reminder.lists.filter.ReminderFilterDialog
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.fragment.dp2px
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.visibleGone
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArchiveFragment : BaseToolbarFragment<FragmentTrashBinding>() {

  private val viewModel by viewModel<ArchiveRemindersViewModel>()

  private val reminderResolver by lazy {
    ReminderActionResolver(
      context = requireContext(),
      dialogues = dialogues,
      permissionFlow = permissionFlow,
      toggleAction = { },
      deleteAction = { viewModel.deleteReminder(it) },
      skipAction = { },
      openAction = {
        navigate {
          navigate(
            R.id.previewReminderFragment,
            Bundle().apply {
              putString(IntentKeys.INTENT_ID, it)
            }
          )
        }
      },
      editAction = {
        navigate {
          navigate(
            R.id.buildReminderFragment,
            Bundle().apply {
              putString(IntentKeys.INTENT_ID, it)
            }
          )
        }
      }
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

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentTrashBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerView.applyBottomInsets()

    binding.reminderSearchBar.doAfterTextChanged {
      viewModel.onSearchUpdate(it?.toString().orEmpty())
    }

    addMenu(R.menu.fragment_reminders_archive, { menuItem ->
      return@addMenu when (menuItem.itemId) {
        R.id.action_delete_all -> {
          viewModel.deleteAll()
          true
        }
        R.id.action_filter -> {
          viewModel.showFilters()
          true
        }
        else -> true
      }
    })

    // Set up result listener for filter selection
    setFragmentResultListener(ReminderFilterDialog.REQUEST_KEY) { _, result ->
      viewModel.handleFilterResult(ReminderFilterDialog.getAppliedFiltersFromResult(result))
    }

    initList()
    initViewModel()
  }

  private fun showFilters(filters: Filters) {
    val dialog = ReminderFilterDialog.newInstance(
      filters = filters,
      title = getString(R.string.filter_reminders)
    )
    dialog.show(parentFragmentManager, "ReminderFilterDialog")
  }

  private fun initViewModel() {
    viewModel.events.nonNullObserve(viewLifecycleOwner) { showData(it) }
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) {
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
    viewModel.showFilters.observeEvent(viewLifecycleOwner) { showFilters(it) }
    viewModel.canFilter.observe(viewLifecycleOwner) { canFilter ->
      updateFilterMenuItem(canFilter == true)
    }
    viewModel.canSearch.observe(viewLifecycleOwner) { canSearch ->
      binding.reminderSearchBar.visibleGone(canSearch == true)
    }
    viewModel.isInProgress.observe(viewLifecycleOwner) {
      binding.linearProgressIndicator.visibility = if (it == true) View.VISIBLE else View.INVISIBLE
    }
    viewModel.canDeleteAll.observe(viewLifecycleOwner) { canDeleteAll ->
      updateDeleteAllMenuItem(canDeleteAll == true)
    }
    lifecycle.addObserver(viewModel)
  }

  private fun updateFilterMenuItem(canFilter: Boolean) {
    updateMenuItem(R.id.action_filter) {
      isVisible = canFilter
    }
  }

  private fun updateDeleteAllMenuItem(canDeleteAll: Boolean) {
    updateMenuItem(R.id.action_delete_all) {
      isVisible = canDeleteAll
    }
  }

  override fun getTitle(): String = getString(R.string.reminders_archive)

  private fun showData(result: List<UiReminderEventsList>) {
    remindersAdapter.submitList(result)
    binding.recyclerView.smoothScrollToPosition(0)
    reloadView(result.size)
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
