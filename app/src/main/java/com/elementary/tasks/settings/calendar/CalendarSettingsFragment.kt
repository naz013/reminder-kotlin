package com.elementary.tasks.settings.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.androidx.viewmodel.ext.android.viewModel

class CalendarSettingsFragment : BaseComposeToolbarFragment() {

  private val viewModel by viewModel<CalendarSettingsViewModel>()

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }
    viewModel.showSelectGoogleCalendarDialog.observeEvent(viewLifecycleOwner) {
      showGoogleCalendarSelectionDialog(it)
    }

    CalendarSettingsScreen(
      state = state,
      onFirstDayClick = viewModel::onFirstDayClick,
      onFirstDayOptionSelected = viewModel::onFirstDayOptionSelected,
      onTodayColorClick = viewModel::onTodayColorClick,
      onReminderColorClick = viewModel::onReminderColorClick,
      onBirthdayColorClick = viewModel::onBirthdayColorClick,
      onSelectCalendarClick = ::requestCalendarPermissionThenSelect,
      onCalendarResetClick = viewModel::onCalendarReset,
      onExportToggle = viewModel::onExportToggle,
      onScanToggle = viewModel::onScanToggle,
      onDialogDismiss = viewModel::onDialogDismiss,
    )
  }

  private fun requestCalendarPermissionThenSelect() {
    permissionFlow.askPermissions(listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      viewModel.onSelectGoogleCalendarClicked()
    }
  }

  private fun handleEvent(event: CalendarSettingsEvent) {
    when (event) {
      is CalendarSettingsEvent.ShowColorPicker -> {
        withActivity { act ->
          dialogues.showColorDialog(
            act,
            event.currentColorIndex,
            event.title,
            ThemeProvider.colorsForSliderThemed(act),
          ) { color -> viewModel.onColorSelected(event.target, color) }
        }
      }
    }
  }

  private fun showGoogleCalendarSelectionDialog(data: CalendarSettingsViewModel.ShowSelectGoogleCalendarDialog) {
    val names = data.calendars.map { it.name }.toTypedArray()
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(R.string.choose_calendar)
    var selectedPosition = data.selectedPosition
    builder.setSingleChoiceItems(names, data.selectedPosition) { _, i -> selectedPosition = i }
    builder.setPositiveButton(R.string.save) { dialog, _ ->
      viewModel.onCalendarSelected(selectedPosition)
      dialog.dismiss()
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    builder.create().show()
  }

  override fun getTitle(): String = arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) ?: getString(R.string.calendar)

  override fun getNavigationIcon(): Int =
    if (arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) == null) {
      super.getNavigationIcon()
    } else {
      R.drawable.ic_builder_clear
    }
}
