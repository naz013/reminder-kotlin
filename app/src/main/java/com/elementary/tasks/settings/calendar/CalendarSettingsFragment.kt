package com.elementary.tasks.settings.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.databinding.FragmentSettingsCalendarBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.github.naz013.common.Permissions
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.databinding.DialogWithSeekAndTitleBinding
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.android.ext.android.inject
import java.util.Locale

class CalendarSettingsFragment : BaseSettingsFragment<FragmentSettingsCalendarBinding>() {

  private val googleCalendarUtils by inject<GoogleCalendarUtils>()

  private var mItemSelect: Int = 0
  private var mDataList: MutableList<GoogleCalendarUtils.CalendarItem> = mutableListOf()
  private val currentPosition: Int
    get() {
      return findPosition(mDataList)
    }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsCalendarBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initFirstDayPrefs()
    binding.eventsImportPrefs.setOnClickListener {
      safeNavigation {
        CalendarSettingsFragmentDirections.actionCalendarSettingsFragmentToFragmentEventsImport()
      }
    }

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

    initExportToCalendarPrefs()
    initEventDurationPrefs()
    initSelectCalendarPrefs()
    initExportToStockPrefs()
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
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setCancelable(true)
      builder.setTitle(getString(R.string.first_day))
      val items = arrayOf(getString(R.string.sunday), getString(R.string.monday))
      mItemSelect = prefs.startDay
      builder.setSingleChoiceItems(items, mItemSelect) { _, which -> mItemSelect = which }
      builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
        prefs.startDay = mItemSelect
        showFirstDay()
        dialogInterface.dismiss()
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
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

  private fun initExportToStockPrefs() {
    binding.exportToStockPrefs.isChecked = prefs.isStockCalendarEnabled
    binding.exportToStockPrefs.setOnClickListener { changeExportToStockPrefs() }
  }

  private fun changeExportToStockPrefs() {
    val isChecked = binding.exportToStockPrefs.isChecked
    binding.exportToStockPrefs.isChecked = !isChecked
    prefs.isStockCalendarEnabled = !isChecked
  }

  private fun initSelectCalendarPrefs() {
    binding.selectCalendarPrefs.setOnClickListener { tryToShowSelectCalendarDialog() }
    binding.selectCalendarPrefs.setDependentView(binding.exportToCalendarPrefs)
    showCurrentCalendar()
  }

  private fun initEventDurationPrefs() {
    binding.eventDurationPrefs.setOnClickListener { showEventDurationDialog() }
    binding.eventDurationPrefs.setDependentView(binding.exportToCalendarPrefs)
    showEventDuration()
  }

  private fun showEventDuration() {
    binding.eventDurationPrefs.setDetailText(
      String.format(
        Locale.getDefault(),
        getString(R.string.x_minutes),
        prefs.calendarEventDuration.toString()
      )
    )
  }

  private fun showEventDurationDialog() {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(R.string.event_duration)
    val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)

    b.seekBar.addOnChangeListener { _, value, _ ->
      b.titleView.text = String.format(
        Locale.getDefault(),
        getString(R.string.x_minutes),
        value.toInt().toString()
      )
    }
    b.seekBar.stepSize = 1f
    b.seekBar.valueFrom = 0f
    b.seekBar.valueTo = 120f

    val duration = prefs.calendarEventDuration
    b.seekBar.value = duration.toFloat()

    b.titleView.text = String.format(
      Locale.getDefault(),
      getString(R.string.x_minutes),
      duration.toString()
    )
    builder.setView(b.root)
    builder.setPositiveButton(R.string.ok) { _, _ ->
      prefs.calendarEventDuration = b.seekBar.value.toInt()
      showEventDuration()
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
    val dialog = builder.create()
    dialog.show()
    Dialogues.setFullWidthDialog(dialog, requireActivity())
  }

  private fun changeExportToCalendarPrefs() {
    permissionFlow.askPermissions(listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      val isChecked = binding.exportToCalendarPrefs.isChecked
      binding.exportToCalendarPrefs.isChecked = !isChecked
      prefs.isCalendarEnabled = !isChecked
      if (binding.exportToCalendarPrefs.isChecked && !showSelectCalendarDialog()) {
        prefs.isCalendarEnabled = false
        binding.exportToCalendarPrefs.isChecked = false
      }
    }
  }

  private fun tryToShowSelectCalendarDialog() {
    permissionFlow.askPermissions(listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      showSelectCalendarDialog()
    }
  }

  private fun showSelectCalendarDialog(): Boolean {
    mDataList.clear()
    mDataList.addAll(googleCalendarUtils.getCalendarsList())
    if (mDataList.isEmpty()) {
      return false
    }
    val names = mDataList.map { it.name }.toTypedArray()
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(R.string.choose_calendar)
    mItemSelect = currentPosition
    builder.setSingleChoiceItems(names, mItemSelect) { _, i ->
      mItemSelect = i
    }
    builder.setPositiveButton(R.string.save) { dialog, _ ->
      if (mItemSelect != -1 && mItemSelect < mDataList.size) {
        prefs.defaultCalendarId = mDataList[mItemSelect].id
      }
      dialog.dismiss()
      showCurrentCalendar()
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
    return true
  }

  private fun showCurrentCalendar() {
    val calendars = googleCalendarUtils.getCalendarsList()
    val pos = findPosition(calendars)
    if (calendars.isNotEmpty() && pos != -1 && pos < calendars.size) {
      val name = calendars[pos].name
      binding.selectCalendarPrefs.setDetailText(name)
    } else {
      binding.selectCalendarPrefs.setDetailText(null)
    }
  }

  private fun initExportToCalendarPrefs() {
    binding.exportToCalendarPrefs.setOnClickListener { changeExportToCalendarPrefs() }
    binding.exportToCalendarPrefs.isChecked = prefs.isCalendarEnabled
  }

  private fun findPosition(list: List<GoogleCalendarUtils.CalendarItem>): Int {
    if (list.isEmpty()) return -1
    val id = prefs.defaultCalendarId
    for (i in list.indices) {
      val item = list[i]
      if (item.id == id) {
        return i
      }
    }
    return -1
  }
}
