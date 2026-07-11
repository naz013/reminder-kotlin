package com.elementary.tasks.home.eventsview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdaysNavKey
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.groups.GroupsNavKey
import com.elementary.tasks.navigation.nav3.AppNavBridge
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.navigation.topfragment.RootFragment
import com.elementary.tasks.reminder.build.BuildReminderNavKey
import com.elementary.tasks.reminder.lists.removed.RemindersArchiveNavKey
import com.elementary.tasks.reminder.preview.ReminderPreviewNavKey
import com.github.naz013.common.Permissions
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeEventsFragment :
  Fragment(),
  RootFragment {
  private val viewModel by viewModel<EventsViewModel>()
  private val dialogues by inject<Dialogues>()
  private val appNavBridge by inject<AppNavBridge>()
  private lateinit var permissionFlow: PermissionFlow

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionFlow = PermissionFlow(this, dialogues)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val state by viewModel.eventsScreenState.collectAsState()
      EventsScreen(
        state = state,
        onBackClick = { requireActivity().onBackPressedDispatcher.onBackPressed() },
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategoryToggle = viewModel::onCategoryToggle,
        onAddReminderClick = viewModel::onAddReminderClick,
        onAddShoppingClick = viewModel::onAddShoppingClick,
        onAddBirthdayClick = viewModel::onAddBirthdayClick,
        onArchiveClick = viewModel::onArchiveClick,
        onGroupsClick = viewModel::onGroupsClick,
        onItemClick = viewModel::onItemClick,
        onEventMenuAction = viewModel::onEventMenuAction,
      )
    }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    lifecycle.addObserver(viewModel)
    viewModel.navigationEvent.observeEvent(viewLifecycleOwner) { handleNavigationEvent(it) }
  }

  /** Registers this fragment as the Activity's "current fragment" for hardware/gesture back-press
   *  routing (see [com.elementary.tasks.home.BottomNavActivity.handleBackPress]). */
  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  private fun handleNavigationEvent(event: EventsViewModel.NavigationEvent) {
    when (event) {
      is EventsViewModel.NavigationEvent.OpenReminderPreview -> {
        appNavBridge.navigate(ReminderPreviewNavKey.Preview(event.id))
      }

      is EventsViewModel.NavigationEvent.OpenReminderEdit -> {
        appNavBridge.navigate(BuildReminderNavKey.Main(id = event.id))
      }

      EventsViewModel.NavigationEvent.OpenNewReminder -> {
        appNavBridge.navigate(BuildReminderNavKey.Main())
      }

      EventsViewModel.NavigationEvent.OpenNewShoppingReminder -> {
        appNavBridge.navigate(BuildReminderNavKey.Main(deepLinkTodo = true))
      }

      is EventsViewModel.NavigationEvent.OpenBirthdayPreview -> {
        appNavBridge.navigate(BirthdaysNavKey.Preview(event.id))
      }

      is EventsViewModel.NavigationEvent.OpenBirthdayEdit -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit(event.id))
      }

      EventsViewModel.NavigationEvent.OpenNewBirthday -> {
        appNavBridge.navigate(BirthdaysNavKey.Edit())
      }

      EventsViewModel.NavigationEvent.OpenArchive -> {
        appNavBridge.navigate(RemindersArchiveNavKey.List)
      }

      EventsViewModel.NavigationEvent.OpenGroups -> {
        appNavBridge.navigate(GroupsNavKey.List)
      }

      is EventsViewModel.NavigationEvent.RequestGpsPermission -> {
        permissionFlow.askPermissions(
          listOf(Permissions.FOREGROUND_SERVICE, Permissions.FOREGROUND_SERVICE_LOCATION),
        ) {
          viewModel.toggleReminder(event.id)
        }
      }

      is EventsViewModel.NavigationEvent.ConfirmArchiveReminder -> {
        dialogues.askConfirmation(requireContext(), getString(R.string.move_to_archive)) { confirmed ->
          if (confirmed) viewModel.moveReminderToArchive(event.id)
        }
      }

      is EventsViewModel.NavigationEvent.ConfirmDeleteReminder -> {
        dialogues.askConfirmation(requireContext(), getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deleteReminder(event.id)
        }
      }

      is EventsViewModel.NavigationEvent.ConfirmDeleteBirthday -> {
        dialogues.askConfirmation(requireContext(), getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deleteBirthday(event.id)
        }
      }
    }
  }
}
