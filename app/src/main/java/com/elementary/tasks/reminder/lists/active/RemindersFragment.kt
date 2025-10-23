package com.elementary.tasks.reminder.lists.active

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.views.recyclerview.SpaceBetweenItemDecoration
import com.elementary.tasks.databinding.FragmentRemindersBinding
import com.elementary.tasks.home.eventsview.BaseSubEventsFragment
import com.elementary.tasks.home.eventsview.HomeEventsFragmentDirections
import com.elementary.tasks.reminder.lists.ReminderActionResolver
import com.elementary.tasks.reminder.lists.RemindersAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList
import com.elementary.tasks.reminder.lists.filter.Filters
import com.elementary.tasks.reminder.lists.filter.ReminderFilterDialog
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.dp2px
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.visibleGone
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemindersFragment : BaseSubEventsFragment<FragmentRemindersBinding>() {

  private val viewModel by viewModel<ActiveRemindersViewModel>()

  private val reminderResolver by lazy {
    ReminderActionResolver(
      context = requireContext(),
      dialogues = dialogues,
      permissionFlow = permissionFlow,
      toggleAction = { viewModel.toggleReminder(it) },
      deleteAction = { viewModel.moveToTrash(it) },
      skipAction = { viewModel.skip(it) },
      openAction = {
        Logger.d(TAG, "Open reminder with id: $it")
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
    isEditable = true,
    onItemClicked = { reminderResolver.resolveItemClick(it.id, it.state.isRemoved) },
    onToggleClicked = { reminderResolver.resolveItemToggle(it.id, it.state.isGps) },
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
  ) = FragmentRemindersBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerView.applyBottomInsets()
    binding.fab.setOnClickListener {
      navigate {
        navigate(R.id.buildReminderFragment)
      }
    }

    analyticsEventSender.send(ScreenUsedEvent(Screen.REMINDERS_LIST))
    binding.reminderSearchBar.doAfterTextChanged {
      viewModel.onSearchUpdate(it?.toString().orEmpty())
    }

    initList()
    initViewModel()

    // Set up result listener for filter selection
    setFragmentResultListener(ReminderFilterDialog.REQUEST_KEY) { _, result ->
      viewModel.handleFilterResult(ReminderFilterDialog.getAppliedFiltersFromResult(result))
    }
  }

  override fun onResume() {
    super.onResume()
    addMenu(R.menu.fragment_reminders, { menuItem ->
      when (menuItem.itemId) {
        R.id.action_map -> {
          safeNavigation { HomeEventsFragmentDirections.actionActionEventsToMapFragment() }
        }
        R.id.action_groups -> {
          safeNavigation { HomeEventsFragmentDirections.actionActionEventsToGroupsFragment() }
        }
        R.id.action_archive -> {
          safeNavigation { HomeEventsFragmentDirections.actionActionEventsToArchiveFragment() }
        }
        R.id.action_settings -> {
          safeNavigation {
            HomeEventsFragmentDirections.actionActionEventsToRemindersSettingsFragment()
          }
        }
        R.id.action_filter -> {
          viewModel.showFilters()
        }
        else -> {
          false
        }
      }
      true
    })
  }

  private fun showFilters(filters: Filters) {
    Logger.i(TAG, "Showing filters: ${filters.filterGroups.size}")
    val dialog = ReminderFilterDialog.newInstance(
      filters = filters,
      title = getString(R.string.filter_reminders)
    )
    dialog.show(parentFragmentManager, "ReminderFilterDialog")
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.events.nonNullObserve(viewLifecycleOwner) { showData(it) }
    viewModel.errorEvent.observeEvent(viewLifecycleOwner) { toast(it) }
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) {
      if (it == Commands.OUTDATED) {
        toast(R.string.reminder_is_outdated)
      }
    }
    viewModel.showFilters.observeEvent(viewLifecycleOwner) { showFilters(it) }
    viewModel.canFilter.observe(viewLifecycleOwner) { canFilter ->
      updateFilterMenuIcon(canFilter == true)
    }
    viewModel.canSearch.observe(viewLifecycleOwner) { canSearch ->
      binding.reminderSearchBar.visibleGone(canSearch == true)
    }
    viewModel.isInProgress.observe(viewLifecycleOwner) {
      binding.linearProgressIndicator.visibility = if (it == true) View.VISIBLE else View.INVISIBLE
    }
  }

  private fun updateFilterMenuIcon(canFilter: Boolean) {
    fragmentMenuController?.updateMenuItem(R.id.action_filter) {
      isVisible = canFilter
    }
  }

  private fun showData(result: List<UiReminderEventsList>) {
    remindersAdapter.submitList(result)
    binding.recyclerView.smoothScrollToPosition(0)
    reloadEmptyView(result.size)
  }

  private fun initList() {
    binding.recyclerView.layoutManager = LinearLayoutManager(context)
    binding.recyclerView.adapter = remindersAdapter
    binding.recyclerView.addItemDecoration(SpaceBetweenItemDecoration(dp2px(8)))
    ViewUtils.listenScrollableView(binding.recyclerView) {
      if (it) {
        binding.fab.show()
      } else {
        binding.fab.hide()
      }
    }
    reloadEmptyView(0)
  }

  private fun reloadEmptyView(count: Int) {
    binding.emptyItem.visibleGone(count == 0)
  }

  companion object {
    private const val TAG = "RemindersFragment"
  }
}
