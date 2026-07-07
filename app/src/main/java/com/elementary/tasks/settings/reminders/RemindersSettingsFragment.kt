package com.elementary.tasks.settings.reminders

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.elementary.tasks.R
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.navigate
import com.elementary.tasks.navigation.safeNavigation
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.common.Module
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RemindersSettingsFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<RemindersSettingsViewModel>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }
    val hasLocation = remember { Module.hasLocation(requireContext()) }

    RemindersSettingsScreen(
      state = state,
      hasLocation = hasLocation,
      onPresetsClick = viewModel::onPresetsClick,
      onLocationClick = viewModel::onLocationClick,
      onPriorityClick = viewModel::onPriorityClick,
      onCompletedToggle = viewModel::onCompletedToggle,
      onWearToggle = viewModel::onWearToggle,
      onSnoozeClick = viewModel::onSnoozeClick,
      onRepeatToggle = viewModel::onRepeatToggle,
      onRepeatIntervalClick = viewModel::onRepeatIntervalClick,
      onLedToggle = viewModel::onLedToggle,
      onLedColorClick = viewModel::onLedColorClick,
      onPermanentNotificationClick = ::togglePermanentNotification,
      onStatusIconToggle = viewModel::onStatusIconToggle,
      onDoNotDisturbToggle = viewModel::onDoNotDisturbToggle,
      onDndFromClick = viewModel::onDndFromClick,
      onDndToClick = viewModel::onDndToClick,
      onDndActionClick = viewModel::onDndActionClick,
      onDndIgnoreClick = viewModel::onDndIgnoreClick,
      onChoiceOptionSelected = viewModel::onChoiceOptionSelected,
      onSeekValueChange = viewModel::onSeekValueChange,
      onSeekConfirm = viewModel::onSeekConfirm,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }

  private fun togglePermanentNotification() {
    val turningOn = !viewModel.state.value.isPermanentNotificationChecked
    if (turningOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionFlow.askPermission(Permissions.POST_NOTIFICATION) { viewModel.onPermanentNotificationToggle() }
    } else {
      viewModel.onPermanentNotificationToggle()
    }
  }

  private fun handleEvent(event: RemindersSettingsEvent) {
    when (event) {
      RemindersSettingsEvent.OpenPresets -> {
        safeNavigation { RemindersSettingsFragmentDirections.actionRemindersSettingsFragmentToManagePresetsFragment() }
      }

      RemindersSettingsEvent.OpenLocationSettings -> {
        navigate { navigate(R.id.locationSettingsFragment, null, NavigationAnimations.inDepthNavOptions()) }
      }

      is RemindersSettingsEvent.ShowTimePicker -> {
        val titleRes = if (event.target == DndTimeTarget.FROM) R.string.from else R.string.to
        dateTimePickerProvider.showTimePicker(
          fragmentManager = childFragmentManager,
          time = event.time,
          title = getString(titleRes),
        ) { viewModel.onTimeSelected(event.target, it) }
      }

      RemindersSettingsEvent.ShowPermanentNotification -> PermanentReminderReceiver.show(requireContext())

      RemindersSettingsEvent.HidePermanentNotification -> PermanentReminderReceiver.hide(requireContext())
    }
  }

  override fun getTitle(): String = arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) ?: getString(R.string.reminders_)

  override fun getNavigationIcon(): Int =
    if (arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) == null) {
      super.getNavigationIcon()
    } else {
      R.drawable.ic_builder_clear
    }
}
