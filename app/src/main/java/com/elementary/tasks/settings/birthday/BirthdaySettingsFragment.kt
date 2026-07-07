package com.elementary.tasks.settings.birthday

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class BirthdaySettingsFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<BirthdaySettingsViewModel>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }

    BirthdaySettingsScreen(
      state = state,
      onReminderToggle = viewModel::onReminderToggle,
      onDaysToBirthdayClick = viewModel::onDaysToBirthdayClick,
      onDaysToBirthdayPreviewChange = viewModel::onDaysToBirthdayPreviewChange,
      onDaysToBirthdayConfirm = viewModel::onDaysToBirthdayConfirm,
      onPriorityClick = viewModel::onPriorityClick,
      onPriorityOptionSelected = viewModel::onPriorityOptionSelected,
      onReminderTimeClick = viewModel::onReminderTimeClick,
      onWidgetToggle = viewModel::onWidgetToggle,
      onHomeDaysClick = viewModel::onHomeDaysClick,
      onHomeDaysPreviewChange = viewModel::onHomeDaysPreviewChange,
      onHomeDaysConfirm = viewModel::onHomeDaysConfirm,
      onPermanentToggle = viewModel::onPermanentToggle,
      onGlobalToggle = viewModel::onGlobalToggle,
      onLedToggle = viewModel::onLedToggle,
      onLedColorClick = viewModel::onLedColorClick,
      onLedColorOptionSelected = viewModel::onLedColorOptionSelected,
      onUseContactsToggle = ::requestContactsThenToggle,
      onAutoScanToggle = viewModel::onAutoScanToggle,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }

  private fun requestContactsThenToggle() {
    permissionFlow.askPermission(Permissions.READ_CONTACTS) {
      viewModel.onUseContactsToggle()
    }
  }

  private fun handleEvent(event: BirthdaySettingsEvent) {
    when (event) {
      is BirthdaySettingsEvent.ShowTimePicker -> {
        dateTimePickerProvider.showTimePicker(
          fragmentManager = childFragmentManager,
          time = event.time,
          title = getString(R.string.remind_at),
        ) { viewModel.onTimeSelected(it) }
      }

      is BirthdaySettingsEvent.UpdatePermanentNotificationVisibility -> {
        val action =
          if (event.visible) {
            PermanentBirthdayReceiver.ACTION_SHOW
          } else {
            PermanentBirthdayReceiver.ACTION_HIDE
          }
        requireActivity().sendBroadcast(
          Intent(requireContext(), PermanentBirthdayReceiver::class.java).setAction(action),
        )
      }
    }
  }

  override fun getTitle(): String = arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) ?: getString(R.string.birthdays)

  override fun getNavigationIcon(): Int =
    if (arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) == null) {
      super.getNavigationIcon()
    } else {
      R.drawable.ic_builder_clear
    }
}
