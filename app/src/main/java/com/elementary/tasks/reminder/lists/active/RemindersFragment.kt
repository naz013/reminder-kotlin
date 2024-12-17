package com.elementary.tasks.reminder.lists.active

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.os.dp2px
import com.elementary.tasks.core.os.toast
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.applyBottomInsets
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.core.views.recyclerview.SpaceBetweenItemDecoration
import com.elementary.tasks.databinding.FragmentRemindersBinding
import com.elementary.tasks.home.eventsview.BaseSubEventsFragment
import com.elementary.tasks.home.eventsview.HomeEventsFragmentDirections
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import com.elementary.tasks.reminder.lists.ReminderActionResolver
import com.elementary.tasks.reminder.lists.RemindersAdapter
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList
import com.github.naz013.logging.Logger
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemindersFragment : BaseSubEventsFragment<FragmentRemindersBinding>() {

  private val systemServiceProvider by inject<SystemServiceProvider>()
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()
  private val viewModel by viewModel<ActiveRemindersViewModel>()

  private val reminderResolver by lazy {
    ReminderActionResolver(
      context = requireContext(),
      dialogues = dialogues,
      reminderBuilderLauncher = reminderBuilderLauncher,
      permissionFlow = permissionFlow,
      toggleAction = { viewModel.toggleReminder(it) },
      deleteAction = { viewModel.moveToTrash(it) },
      skipAction = { viewModel.skip(it) }
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

  private val searchMenuHandler = SearchMenuHandler(
    systemServiceProvider.provideSearchManager(),
    R.string.search
  ) {
    viewModel.onSearchUpdate(it)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentRemindersBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerView.applyBottomInsets()
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
      }
      true
    }) {
      searchMenuHandler.initSearchMenu(requireActivity(), it, R.id.action_search)
    }

    binding.fab.setOnClickListener {
      reminderBuilderLauncher.openLogged(requireContext()) { }
    }

    analyticsEventSender.send(ScreenUsedEvent(Screen.REMINDERS_LIST))

    initList()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.events.nonNullObserve(viewLifecycleOwner) { showData(it) }
    viewModel.error.nonNullObserve(viewLifecycleOwner) {
      Logger.d("initViewModel: onError -> $it")
      toast(it)
    }
    viewModel.result.nonNullObserve(viewLifecycleOwner) {
      if (it == Commands.OUTDATED) {
        toast(R.string.reminder_is_outdated)
      }
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
}
