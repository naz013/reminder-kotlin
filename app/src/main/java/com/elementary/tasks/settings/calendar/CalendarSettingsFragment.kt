package com.elementary.tasks.settings.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsCalendarBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.androidx.viewmodel.ext.android.viewModel

class CalendarSettingsFragment : BaseSettingsFragment<FragmentSettingsCalendarBinding>() {

  private val viewModel by viewModel<CalendarSettingsViewModel>()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsCalendarBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initFirstDayPrefs()

    binding.reminderColorPrefs.setOnClickListener {
      showColorPopup(prefs.reminderColor, getString(R.string.reminders_color)) { color ->
        prefs.reminderColor = color
        initRemindersColorPrefs()
      }
    }
    initRemindersColorPrefs()

    binding.todayColorPrefs.setOnClickListener {
      showColorPopup(prefs.todayColor, getString(R.string.today_color)) { color ->
        prefs.todayColor = color
        initTodayColorPrefs()
      }
    }
    initTodayColorPrefs()

    binding.birthdayColorPrefs.setOnClickListener {
      showColorPopup(prefs.birthdayColor, getString(R.string.birthdays_color)) { color ->
        prefs.birthdayColor = color
        initBirthdaysColorPrefs()
      }
    }
    initBirthdaysColorPrefs()

    initGoogleCalendarIdPrefs()
    initAddRemindersToGoogleCalendarPrefs()
    initScanGoogleCalendarPrefs()

    initViewModel()
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
  }

  private fun initGoogleCalendarIdPrefs() {
    viewModel.selectedCalendar.observe(viewLifecycleOwner) {
      binding.selectCalendarPrefs.setDetailText(it.name)
      binding.exportToCalendarPrefs.setDependentValue(it?.id != -1L)
      binding.scanCalendarPrefs.setDependentValue(it?.id != -1L)
      binding.selectCalendarPrefs.setCustomButtonVisible(it?.id != -1L)
    }
    viewModel.showSelectGoogleCalendarDialog.observeEvent(viewLifecycleOwner) {
      showGoogleCalendarSelectionDialog(it)
    }
    binding.selectCalendarPrefs.setOnClickListener {
      permissionFlow.askPermissions(listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
        viewModel.onSelectGoogleCalendarClicked()
      }
    }
    binding.selectCalendarPrefs.setCustomButton(getString(R.string.reset_calendar)) {
      viewModel.onCalendarReset()
    }
  }

  private fun showGoogleCalendarSelectionDialog(
    data: CalendarSettingsViewModel.ShowSelectGoogleCalendarDialog
  ) {
    val names = data.calendars.map { it.name }.toTypedArray()
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(R.string.choose_calendar)
    var selectedPosition = data.selectedPosition
    builder.setSingleChoiceItems(names, data.selectedPosition) { _, i ->
      selectedPosition = i
    }
    builder.setPositiveButton(R.string.save) { dialog, _ ->
      viewModel.onCalendarSelected(selectedPosition)
      dialog.dismiss()
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun initAddRemindersToGoogleCalendarPrefs() {
    binding.exportToCalendarPrefs.setOnClickListener {
      changeAddRemindersToGoogleCalendarPrefs()
    }
    binding.exportToCalendarPrefs.isChecked = prefs.addRemindersToGoogleCalendar
  }

  private fun changeAddRemindersToGoogleCalendarPrefs() {
    prefs.addRemindersToGoogleCalendar = !prefs.addRemindersToGoogleCalendar
    binding.exportToCalendarPrefs.isChecked = prefs.addRemindersToGoogleCalendar
  }

  private fun initScanGoogleCalendarPrefs() {
    binding.scanCalendarPrefs.setOnClickListener { changeScanGoogleCalendarPrefs() }
    binding.scanCalendarPrefs.isChecked = prefs.scanGoogleCalendarEvents
  }

  private fun changeScanGoogleCalendarPrefs() {
    prefs.scanGoogleCalendarEvents = !prefs.scanGoogleCalendarEvents
    binding.scanCalendarPrefs.isChecked = prefs.scanGoogleCalendarEvents
  }

  private fun showColorPopup(current: Int, title: String, onSave: (Int) -> Unit) {
    withActivity { act ->
      dialogues.showColorDialog(act, current, title, ThemeProvider.colorsForSliderThemed(act)) {
        onSave.invoke(it)
      }
    }
  }

  private fun initFirstDayPrefs() {
    binding.startDayPrefs.setOnClickListener { showFirstDayDialog() }
    showFirstDay()
  }

  private fun showFirstDay() {
    val items = arrayOf(getString(R.string.sunday), getString(R.string.monday))
    binding.startDayPrefs.setDetailText(items[prefs.startDay])
  }

  private fun showFirstDayDialog() {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setCancelable(true)
    builder.setTitle(getString(R.string.first_day_of_the_week))
    val items = arrayOf(getString(R.string.sunday), getString(R.string.monday))
    var selectedPosition = prefs.startDay
    builder.setSingleChoiceItems(items, selectedPosition) { _, which -> selectedPosition = which }
    builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
      prefs.startDay = selectedPosition
      showFirstDay()
      dialogInterface.dismiss()
    }
    builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun initRemindersColorPrefs() {
    binding.reminderColorPrefs.setViewColor(
      ThemeProvider.colorReminderCalendar(requireContext(), prefs.reminderColor)
    )
  }

  override fun getTitle(): String {
    return arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) ?: getString(R.string.calendar)
  }

  override fun getNavigationIcon(): Int {
    return if (arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) == null) {
      super.getNavigationIcon()
    } else {
      R.drawable.ic_builder_clear
    }
  }

  private fun initBirthdaysColorPrefs() {
    binding.birthdayColorPrefs.setViewColor(
      ThemeProvider.colorBirthdayCalendar(requireContext(), prefs.birthdayColor)
    )
  }

  private fun initTodayColorPrefs() {
    binding.todayColorPrefs.setViewColor(
      ThemeProvider.colorTodayCalendar(requireContext(), prefs.todayColor)
    )
  }
}
