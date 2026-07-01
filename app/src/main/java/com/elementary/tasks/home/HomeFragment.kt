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
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.compose.composeView
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
      }
    }
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  /**

  private fun setUpWhatsNewBanner() {
    binding.whatsNewOkButton.setOnClickListener {
      whatsNewManager.hideWhatsNew()
    }
    binding.whatsNewReadMoreButton.setOnClickListener {
      whatsNewManager.hideWhatsNew()
      analyticsEventSender.send(ScreenUsedEvent(Screen.WHATS_NEW))
      safeNavigation(HomeFragmentDirections.actionActionHomeToChangesFragment())
    }
  }

  private fun updatePrivacyBanner() {
    if (prefs.isPrivacyPolicyShowed) {
      binding.privacyBanner.gone()
    } else {
      binding.privacyBanner.visible()
      binding.privacyButton.setOnClickListener {
        startActivity(PrivacyPolicyActivity::class.java)
      }
      binding.acceptButton.setOnClickListener { prefs.isPrivacyPolicyShowed = true }
    }
  }

  private fun updateLoginBanner() {
    if (prefs.isPrivacyPolicyShowed) {
      if (prefs.isUserLogged ||
        !featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_DRIVE)
      ) {
        binding.loginBanner.gone()
      } else {
        binding.loginBanner.visible()
        binding.loginDismissButton.setOnClickListener { prefs.isUserLogged = true }
        binding.loginButton.setOnClickListener {
          prefs.isUserLogged = true
          safeNavigation(HomeFragmentDirections.actionActionHomeToCloudDrives())
        }
      }
    } else {
      binding.loginBanner.gone()
    }
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.scheduleData.nonNullObserve(viewLifecycleOwner) { updateList(it) }

    searchViewModel.searchResults.nonNullObserve(viewLifecycleOwner) {
      searchAdapter.submitList(it)
    }
    searchViewModel.navigateLiveData.nonNullObserve(viewLifecycleOwner) { onNavigationAction(it) }
  }

  private fun onNavigationAction(navigationAction: NavigationAction) {
    when (navigationAction) {
      is ActivityNavigation -> {
        startActivity(navigationAction.clazz) {
          putExtra(IntentKeys.INTENT_ID, navigationAction.objectId)
        }
      }

      is FragmentNavigation -> {
        navigate {
          navigate(
            navigationAction.id,
            Bundle().apply {
              putString(IntentKeys.INTENT_ID, navigationAction.objectId)
            },
            NavigationAnimations.inDepthNavOptions()
          )
        }
      }
    }
  }

  */
}
