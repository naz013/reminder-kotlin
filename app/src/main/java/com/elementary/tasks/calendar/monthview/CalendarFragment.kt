package com.elementary.tasks.calendar.monthview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.core.deeplink.BirthdayDateDeepLinkData
import com.elementary.tasks.core.deeplink.ReminderDatetimeTypeDeepLinkData
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.topfragment.RootFragment
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class CalendarFragment :
  Fragment(),
  RootFragment {
  private val dateTimeManager by inject<DateTimeManager>()

  private val viewModel by viewModel<CalendarViewModel>()

  private var pagerJumpRequest by mutableStateOf<Int?>(null)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View =
    composeView {
      val state by viewModel.state.collectAsState()
      val refreshSignal by viewModel.refreshSignal.collectAsState()
      CalendarScreen(
        state = state,
        initialPagerPosition = viewModel.lastPosition,
        pagerJumpRequest = pagerJumpRequest,
        onPagerJumpConsumed = { pagerJumpRequest = null },
        monthForPosition = viewModel::monthForPosition,
        onPageSettled = { position ->
          viewModel.updateLastPosition(position)
          viewModel.onPageSettled(position)
        },
        buildGrid = viewModel::buildGrid,
        refreshSignal = refreshSignal,
        loadMonthEvents = viewModel::loadMonthEvents,
        onDayClick = viewModel::onDayClick,
        onAddReminderClick = viewModel::onAddReminderClick,
        onAddBirthdayClick = viewModel::onAddBirthdayClick,
        onSettingsClick = viewModel::onSettingsClick,
        onBackClick = { requireActivity().onBackPressedDispatcher.onBackPressed() },
      )
    }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.navigationEvent.observeEvent(viewLifecycleOwner) { event -> handleNavigationEvent(event) }
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
    // Always snap back to the current month when this screen becomes visible, matching the
    // legacy behavior of resetting the pager on every resume (not just after a back-stack pop).
    pagerJumpRequest = CalendarViewModel.CENTER_POSITION
    viewModel.resetToToday()
    viewModel.refresh()
  }

  private fun handleNavigationEvent(event: CalendarViewModel.NavigationEvent) {
    when (event) {
      is CalendarViewModel.NavigationEvent.OpenDayView -> {
        safeNavigation(
          CalendarFragmentDirections.actionActionCalendarToDayViewFragment(
            dateTimeManager.toMillis(LocalDateTime.of(event.date, LocalTime.now())),
          ),
        )
      }

      is CalendarViewModel.NavigationEvent.OpenNewReminder -> {
        val deepLinkData =
          ReminderDatetimeTypeDeepLinkData(
            type = Reminder.BY_DATE,
            dateTime = LocalDateTime.of(event.date, LocalTime.now()),
          )
        safeNavigation(
          R.id.buildReminderFragment,
          Bundle().apply {
            putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
            putParcelable(deepLinkData.intentKey, deepLinkData)
          },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is CalendarViewModel.NavigationEvent.OpenNewBirthday -> {
        val deepLinkData = BirthdayDateDeepLinkData(event.date)
        safeNavigation(
          R.id.birthdayFragment,
          Bundle().apply {
            putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
            putParcelable(deepLinkData.intentKey, deepLinkData)
          },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      CalendarViewModel.NavigationEvent.OpenSettings -> {
        safeNavigation(
          CalendarFragmentDirections.actionActionCalendarToCalendarSettingsFragment(getString(R.string.action_settings)),
        )
      }
    }
  }
}
