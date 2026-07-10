package com.elementary.tasks.calendar.dayview

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
import com.elementary.tasks.birthdays.BirthdaysFragment
import com.elementary.tasks.core.deeplink.BirthdayDateDeepLinkData
import com.elementary.tasks.core.deeplink.ReminderDatetimeTypeDeepLinkData
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.onBackStackResume
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.topfragment.RootFragment
import com.github.naz013.common.Permissions
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.compose.composeView
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class WeekViewFragment :
  Fragment(),
  RootFragment {
  private val dateTimeManager by inject<DateTimeManager>()
  private val dialogues by inject<Dialogues>()
  private lateinit var permissionFlow: PermissionFlow

  private val viewModel by viewModel<WeekViewModel> { parametersOf(getDate()) }

  private var pagerJumpRequest by mutableStateOf<Int?>(null)

  private fun getDate(): LocalDate =
    arguments?.let {
      dateTimeManager.fromMillis(WeekViewFragmentArgs.fromBundle(it).date).toLocalDate()
    } ?: LocalDate.now()

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
      val state by viewModel.state.collectAsState()
      val refreshSignal by viewModel.refreshSignal.collectAsState()
      WeekViewScreen(
        state = state,
        initialPagerPosition = viewModel.lastPosition,
        pagerJumpRequest = pagerJumpRequest,
        onPagerJumpConsumed = { pagerJumpRequest = null },
        dateForPosition = viewModel::dateForPosition,
        onPageSettled = { position ->
          viewModel.updateLastPosition(position)
          viewModel.onDateSelected(viewModel.dateForPosition(position))
        },
        onDayClick = { day -> viewModel.selectDate(day.localDate) },
        refreshSignal = refreshSignal,
        loadDayEvents = viewModel::loadDayEvents,
        onItemClick = viewModel::onItemClick,
        onEventMenuAction = viewModel::onEventMenuAction,
        onAddReminderClick = { viewModel.onAddReminderClick(state.selectedDate) },
        onAddBirthdayClick = { viewModel.onAddBirthdayClick(state.selectedDate) },
        onBackClick = { requireActivity().onBackPressedDispatcher.onBackPressed() },
      )
    }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    lifecycle.addObserver(viewModel)
    viewModel.navigationEvent.observeEvent(viewLifecycleOwner) { event ->
      if (event is WeekViewModel.NavigationEvent.MoveToDate) {
        pagerJumpRequest = viewModel.positionForDate(event.date)
      } else {
        handleNavigationEvent(event)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  private fun handleNavigationEvent(event: WeekViewModel.NavigationEvent) {
    when (event) {
      is WeekViewModel.NavigationEvent.MoveToDate -> Unit

      is WeekViewModel.NavigationEvent.OpenReminderPreview -> {
        safeNavigation(
          R.id.previewReminderFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is WeekViewModel.NavigationEvent.OpenReminderEdit -> {
        safeNavigation(
          R.id.buildReminderFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is WeekViewModel.NavigationEvent.OpenBirthdayPreview -> {
        safeNavigation(
          R.id.birthdayFragment,
          Bundle().apply { putString(IntentKeys.INTENT_ID, event.id) },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is WeekViewModel.NavigationEvent.OpenBirthdayEdit -> {
        safeNavigation(
          R.id.birthdayFragment,
          Bundle().apply {
            putString(IntentKeys.INTENT_ID, event.id)
            putBoolean(BirthdaysFragment.ARG_OPEN_EDIT, true)
          },
          NavigationAnimations.inDepthNavOptions(),
        )
      }

      is WeekViewModel.NavigationEvent.OpenNewReminder -> {
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

      is WeekViewModel.NavigationEvent.OpenNewBirthday -> {
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

      is WeekViewModel.NavigationEvent.ConfirmArchiveReminder -> {
        dialogues.askConfirmation(requireContext(), getString(R.string.move_to_archive)) { confirmed ->
          if (confirmed) viewModel.moveReminderToArchive(event.id)
        }
      }

      is WeekViewModel.NavigationEvent.ConfirmDeleteBirthday -> {
        dialogues.askConfirmation(requireContext(), getString(R.string.delete)) { confirmed ->
          if (confirmed) viewModel.deleteBirthday(event.id)
        }
      }

      is WeekViewModel.NavigationEvent.RequestGpsPermission -> {
        permissionFlow.askPermissions(
          listOf(Permissions.FOREGROUND_SERVICE, Permissions.FOREGROUND_SERVICE_LOCATION),
        ) {
          viewModel.toggleReminder(event.id)
        }
      }
    }
  }
}
