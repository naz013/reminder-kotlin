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
import com.elementary.tasks.core.data.ui.UiReminderList
import com.elementary.tasks.core.data.ui.UiReminderListActiveGps
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentRemindersBinding
import com.elementary.tasks.home.eventsview.BaseSubEventsFragment
import com.elementary.tasks.home.eventsview.HomeEventsFragmentDirections
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.UiReminderListRecyclerAdapter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class RemindersFragment : BaseSubEventsFragment<FragmentRemindersBinding>() {

  private val systemServiceProvider by inject<SystemServiceProvider>()
  private val viewModel by viewModel<ActiveRemindersViewModel>()

  private var mPosition: Int = 0

  private val reminderResolver = ReminderResolver(
    dialogAction = { return@ReminderResolver dialogues },
    toggleAction = { reminder ->
      when (reminder) {
        is UiReminderListActiveGps -> {
          permissionFlow.askPermission(Permissions.FOREGROUND_SERVICE) {
            viewModel.toggleReminder(reminder)
          }
        }

        else -> {
          viewModel.toggleReminder(reminder)
        }
      }
    },
    deleteAction = { reminder -> viewModel.moveToTrash(reminder) },
    skipAction = { reminder -> viewModel.skip(reminder) }
  )

  private val remindersAdapter = UiReminderListRecyclerAdapter(true, isEditable = true)
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
      PinLoginActivity.openLogged(
        requireContext(),
        CreateReminderActivity::class.java
      )
    }

    analyticsEventSender.send(ScreenUsedEvent(Screen.REMINDERS_LIST))

    initList()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.events.nonNullObserve(viewLifecycleOwner) { showData(it) }
    viewModel.error.nonNullObserve(viewLifecycleOwner) {
      Timber.d("initViewModel: onError -> $it")
      toast(it)
    }
    viewModel.result.nonNullObserve(viewLifecycleOwner) {
      if (it == Commands.OUTDATED) {
        remindersAdapter.notifyItemChanged(mPosition)
        toast(R.string.reminder_is_outdated)
      }
    }
  }

  private fun showData(result: List<UiReminderList>) {
    remindersAdapter.submitList(result)
    binding.recyclerView.smoothScrollToPosition(0)
    reloadEmptyView(result.size)
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
          mPosition = position
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
