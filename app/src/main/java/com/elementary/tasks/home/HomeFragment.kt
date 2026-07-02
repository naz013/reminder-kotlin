package com.elementary.tasks.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.topfragment.RootFragment
import com.elementary.tasks.other.PrivacyPolicyActivity
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.compose.composeView
import com.github.naz013.ui.common.fragment.startActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(), RootFragment {

  private val viewModel by viewModel<ScheduleHomeViewModel>()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return composeView {
      val state by viewModel.homeScreenState.collectAsState()
      HomeScreen(
        modifier = Modifier.fillMaxSize(),
        bannerState = state.bannerState,
        onPrivacyPolicyClick = { viewModel.onPrivacyPolicyClick() },
        onPrivacyAcceptClick = { viewModel.onPrivacyAcceptClick() },
        onLoginDismissClick = { viewModel.onLoginDismissClick() },
        onLoginClick = { viewModel.onLoginClick() },
        onWhatsNewDetailsClick = { viewModel.onWhatsNewDetailsClick() },
        onWhatsNewDismissClick = { viewModel.onWhatsNewDismissClick() },
        content = {
          ChronologicalHomeScreen(
            state = state,
            modifier = Modifier
              .fillMaxSize()
              .statusBarsPadding(),
            onSettingsClick = { viewModel.onSettingsClicked() },
            onHeaderNavigationItemClick = { viewModel.onHeaderNavigationItemClicked(it) },
            onEventClick = { viewModel.onEventClicked(it) },
            onEventActionClick = { viewModel.onEventActionClicked(requireContext(), it) },
            onAddMenuItemClick = { viewModel.onEventTypeSelected(it) }
          )
        }
      )
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    lifecycle.addObserver(viewModel)
    viewModel.navigationEvent.observeEvent(viewLifecycleOwner) {
      when (it) {
        is ScheduleHomeViewModel.NavigationEvent.OpenReminderDetails -> {
          safeNavigation(
            R.id.previewReminderFragment,
            Bundle().apply { putString(IntentKeys.INTENT_ID, it.uuid) },
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenBirthdayDetails -> {
          safeNavigation(
            R.id.previewBirthdayFragment,
            Bundle().apply { putString(IntentKeys.INTENT_ID, it.uuid) },
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.ShowEventTypeSelection -> Unit
        is ScheduleHomeViewModel.NavigationEvent.OpenSettings -> {
          safeNavigation(
            R.id.settingsFragment,
            null,
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenCreateReminder -> {
          safeNavigation(
            R.id.buildReminderFragment,
            null,
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenCreateBirthday -> {
          safeNavigation(
            R.id.editBirthdayFragment,
            null,
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenCreateGoogleTask -> {
          safeNavigation(
            R.id.editGoogleTaskFragment,
            null,
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenCalendar -> {
          safeNavigation(
            R.id.actionCalendar,
            null,
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenEvents -> {
          safeNavigation(
            R.id.actionEvents,
            null,
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenNotes -> {
          safeNavigation(
            R.id.actionNotes,
            null,
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenGoogleTasks -> {
          safeNavigation(
            R.id.actionGoogle,
            null,
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenGroups -> {
          safeNavigation(
            R.id.groupsFragment,
            null,
            NavigationAnimations.inDepthNavOptions()
          )
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenPrivacy -> {
          startActivity(PrivacyPolicyActivity::class.java)
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenCloudDrives -> {
          safeNavigation(HomeFragmentDirections.actionActionHomeToCloudDrives())
        }
        is ScheduleHomeViewModel.NavigationEvent.OpenWhatsNew -> {
          safeNavigation(HomeFragmentDirections.actionActionHomeToChangesFragment())
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }
}
