package com.elementary.tasks.home.eventsview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.core.deeplink.ReminderTodoTypeDeepLinkData
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.topfragment.RootFragment
import com.elementary.tasks.reminder.lists.filter.ReminderFilterDialog
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Hosts the merged, chip-filtered Events screen ([EventsScreen]): a single Compose screen
 * replacing the previous TabLayout + ViewPager2 (Reminders / Shopping / Birthdays tabs). This
 * Fragment owns only the Android-framework glue [EventsViewModel] can't (permission requests,
 * confirmation dialogs, the [ReminderFilterDialog] bottom sheet, navigation to sibling
 * destinations) and otherwise just renders [EventsScreen] against [EventsViewModel]'s state.
 */
class HomeEventsFragment :
  Fragment(),
  RootFragment {
  private val viewModel by viewModel<EventsViewModel>()
  private val dialogues by inject<Dialogues>()
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

  /** See [PlacesFragment][com.elementary.tasks.places.list.PlacesFragment]/`GoogleTasksFragment`'s
   *  kdoc: registers this fragment as the Activity's "current fragment" for hardware/gesture
   *  back-press routing (see [com.elementary.tasks.home.BottomNavActivity.handleBackPress]). */
  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  private fun handleNavigationEvent(event: EventsViewModel.NavigationEvent) {
    when (event) {
      is EventsViewModel.NavigationEvent.OpenReminderPreview -> {
        safeNavigation(
          R.id.previewReminderFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is EventsViewModel.NavigationEvent.OpenReminderEdit -> {
        safeNavigation(
          R.id.buildReminderFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      EventsViewModel.NavigationEvent.OpenNewReminder -> {
        safeNavigation(R.id.buildReminderFragment, null, NavigationAnimations.inDepthNavOptions())
      }

      EventsViewModel.NavigationEvent.OpenNewShoppingReminder -> {
        val deepLinkData = ReminderTodoTypeDeepLinkData
        safeNavigation(
          R.id.buildReminderFragment,
          Bundle().apply {
            putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
            putParcelable(deepLinkData.intentKey, deepLinkData)
          },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is EventsViewModel.NavigationEvent.OpenBirthdayPreview -> {
        safeNavigation(
          R.id.previewBirthdayFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is EventsViewModel.NavigationEvent.OpenBirthdayEdit -> {
        safeNavigation(
          R.id.editBirthdayFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      EventsViewModel.NavigationEvent.OpenNewBirthday -> {
        safeNavigation(R.id.editBirthdayFragment, null, NavigationAnimations.inDepthNavOptions())
      }

      EventsViewModel.NavigationEvent.OpenArchive -> {
        safeNavigation(R.id.archiveFragment, null, NavigationAnimations.inDepthNavOptions())
      }

      EventsViewModel.NavigationEvent.OpenGroups -> {
        safeNavigation(R.id.groupsFragment, null, NavigationAnimations.inDepthNavOptions())
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
