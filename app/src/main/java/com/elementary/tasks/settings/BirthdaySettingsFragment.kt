package com.elementary.tasks.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TimePicker
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.work.ScanContactsWorker
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.datetime.TimeUtil
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.birthdays.BirthdaysViewModel
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsBirthdaysSettingsBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class BirthdaySettingsFragment : BaseCalendarFragment<FragmentSettingsBirthdaysSettingsBinding>(),
  TimePickerDialog.OnTimeSetListener {

  private val viewModel by viewModel<BirthdaysViewModel>()
  private val scanContactsWorker by inject<ScanContactsWorker>()
  private val jobScheduler by inject<JobScheduler>()
  private val updatesHelper by inject<UpdatesHelper>()
  private var mItemSelect: Int = 0

  private val onProgress: (Boolean) -> Unit = {
    if (it) {
      binding.progressMessageView.text = getString(R.string.please_wait)
      binding.scanButton.isEnabled = false
      binding.progressView.visibility = View.VISIBLE
    } else {
      binding.progressView.visibility = View.INVISIBLE
      binding.scanButton.isEnabled = true
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsBirthdaysSettingsBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ViewUtils.listenScrollableView(binding.scrollView) {
      setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
    }

    initBirthdayReminderPrefs()
    initBirthdaysWidgetPrefs()
    initPermanentPrefs()
    initDaysToPrefs()
    initHomeDaysPrefs()
    initBirthdayTimePrefs()
    initContactsPrefs()
    initContactsAutoPrefs()
    initNotificationPrefs()
    initViewModel()
    initScanButton()
    initPriority()
  }

  override fun onDestroy() {
    scanContactsWorker.unsubscribe()
    super.onDestroy()
  }

  private fun initPriority() {
    binding.priorityPrefs.setOnClickListener { showPriorityDialog() }
    binding.priorityPrefs.setDependentView(binding.birthReminderPrefs)
    showPriority()
  }

  private fun showPriorityDialog() {
    val builder = dialogues.getMaterialDialog(requireContext())
    builder.setTitle(getString(R.string.default_priority))
    mItemSelect = prefs.birthdayPriority
    builder.setSingleChoiceItems(priorityList(), mItemSelect) { _, which ->
      mItemSelect = which
    }
    builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
      prefs.birthdayPriority = mItemSelect
      showPriority()
      dialog.dismiss()
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun showPriority() {
    binding.priorityPrefs.setDetailText(priorityList()[prefs.birthdayPriority])
  }

  private fun initViewModel() {
    viewModel.result.observe(viewLifecycleOwner) { commands ->
      if (commands != null) {
        when (commands) {
          Commands.DELETED -> {
          }

          else -> {
          }
        }
      }
    }
  }

  private fun initNotificationPrefs() {
    binding.birthdayNotificationPrefs.setOnClickListener {
      safeNavigation(BirthdaySettingsFragmentDirections.actionBirthdaySettingsFragmentToBirthdayNotificationFragment())
    }
    binding.birthdayNotificationPrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun initScanButton() {
    if (prefs.isContactBirthdaysEnabled) {
      binding.scanButton.isEnabled = true
      binding.scanButton.visibility = View.VISIBLE
      binding.scanButton.setOnClickListener { scanForBirthdays() }
      scanContactsWorker.onEnd = {
        val message = if (it == 0) {
          getString(R.string.no_new_birthdays)
        } else {
          getString(R.string.voice_found) + " $it " + getString(R.string.birthdays)
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        onProgress.invoke(false)
      }
      scanContactsWorker.listener = onProgress
    } else {
      binding.scanButton.visibility = View.GONE
    }
  }

  private fun scanForBirthdays() {
    permissionFlow.askPermission(Permissions.READ_CONTACTS) {
      onProgress.invoke(true)
      scanContactsWorker.scan()
    }
  }

  private fun initContactsAutoPrefs() {
    binding.autoScanPrefs.isChecked = prefs.isContactAutoCheckEnabled
    binding.autoScanPrefs.setOnClickListener { changeAutoPrefs() }
    binding.autoScanPrefs.setDependentView(binding.useContactsPrefs)
    binding.autoScanPrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun changeAutoPrefs() {
    val isChecked = binding.autoScanPrefs.isChecked
    binding.autoScanPrefs.isChecked = !isChecked
    prefs.isContactAutoCheckEnabled = !isChecked
    if (!isChecked) {
      jobScheduler.scheduleBirthdaysCheck()
    } else {
      jobScheduler.cancelBirthdaysCheck()
    }
  }

  private fun initContactsPrefs() {
    binding.useContactsPrefs.isChecked = prefs.isContactBirthdaysEnabled
    binding.useContactsPrefs.setOnClickListener { changeContactsPrefs() }
    binding.useContactsPrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun changeContactsPrefs() {
    permissionFlow.askPermission(Permissions.READ_CONTACTS) {
      val isChecked = binding.useContactsPrefs.isChecked
      binding.useContactsPrefs.isChecked = !isChecked
      prefs.isContactBirthdaysEnabled = !isChecked
      initScanButton()
    }
  }

  private fun initBirthdayTimePrefs() {
    binding.reminderTimePrefs.setOnClickListener { showTimeDialog() }
    binding.reminderTimePrefs.setValueText(TimeUtil.getBirthdayVisualTime(prefs.birthdayTime, prefs.is24HourFormat, prefs.appLanguage))
    binding.reminderTimePrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun showTimeDialog() {
    val calendar = TimeUtil.getBirthdayCalendar(prefs.birthdayTime)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    TimeUtil.showTimePicker(requireContext(), prefs.is24HourFormat, hour, minute, this)
  }

  private fun initHomeDaysPrefs() {
    binding.homePrefs.setOnClickListener { showHomeDaysDialog() }
    binding.homePrefs.setDependentView(binding.birthReminderPrefs)
    showHomeDays()
  }

  private fun showHomeDays() {
    binding.homePrefs.setDetailText(homeText(prefs.birthdayDurationInDays))
  }

  private fun homeText(i: Int): String {
    return if (i <= 0) {
      getString(R.string.x_day, "1")
    } else {
      getString(R.string.x_days, (i + 1).toString())
    }
  }

  private fun showHomeDaysDialog() {
    withActivity {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(R.string.birthdays_on_home_for_next)
      val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
      b.seekBar.max = 5
      b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          b.titleView.text = homeText(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
      })
      val days = prefs.birthdayDurationInDays
      b.seekBar.progress = days
      b.titleView.text = homeText(days)
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ -> saveHomeDays(b.seekBar.progress) }
      builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
      val dialog = builder.create()
      dialog.show()
      Dialogues.setFullWidthDialog(dialog, it)
    }
  }

  private fun saveHomeDays(progress: Int) {
    prefs.birthdayDurationInDays = progress
    showHomeDays()
  }

  private fun initDaysToPrefs() {
    binding.daysToPrefs.setOnClickListener { showDaysToDialog() }
    binding.daysToPrefs.setValue(prefs.daysToBirthday)
    binding.daysToPrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun showDaysToDialog() {
    withActivity {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(R.string.days_to_birthday)
      val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
      b.seekBar.max = 5
      b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          b.titleView.text = progress.toString()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
      })
      val daysToBirthday = prefs.daysToBirthday
      b.seekBar.progress = daysToBirthday
      b.titleView.text = daysToBirthday.toString()
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ -> saveDays(b.seekBar.progress) }
      builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
      val dialog = builder.create()
      dialog.show()
      Dialogues.setFullWidthDialog(dialog, it)
    }
  }

  private fun saveDays(progress: Int) {
    prefs.daysToBirthday = progress
    initDaysToPrefs()
  }

  private fun initPermanentPrefs() {
    binding.birthdayPermanentPrefs.isChecked = prefs.isBirthdayPermanentEnabled
    binding.birthdayPermanentPrefs.setOnClickListener { changeBirthdayPermanentPrefs() }
    binding.birthdayPermanentPrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun changeBirthdayPermanentPrefs() {
    val isChecked = binding.birthdayPermanentPrefs.isChecked
    binding.birthdayPermanentPrefs.isChecked = !isChecked
    prefs.isBirthdayPermanentEnabled = !isChecked
    if (!isChecked) {
      requireActivity().sendBroadcast(Intent(requireContext(), PermanentBirthdayReceiver::class.java)
        .setAction(PermanentBirthdayReceiver.ACTION_SHOW))
      jobScheduler.scheduleBirthdayPermanent()
    } else {
      requireActivity().sendBroadcast(Intent(requireContext(), PermanentBirthdayReceiver::class.java)
        .setAction(PermanentBirthdayReceiver.ACTION_HIDE))
      jobScheduler.cancelBirthdayPermanent()
    }
  }

  private fun initBirthdaysWidgetPrefs() {
    binding.widgetShowPrefs.isChecked = prefs.isBirthdayInWidgetEnabled
    binding.widgetShowPrefs.setOnClickListener { changeWidgetPrefs() }
    binding.widgetShowPrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun changeWidgetPrefs() {
    val isChecked = binding.widgetShowPrefs.isChecked
    binding.widgetShowPrefs.isChecked = !isChecked
    prefs.isBirthdayInWidgetEnabled = !isChecked
    updatesHelper.updateCalendarWidget()
    updatesHelper.updateWidgets()
  }

  private fun initBirthdayReminderPrefs() {
    binding.birthReminderPrefs.setOnClickListener { changeBirthdayPrefs() }
    binding.birthReminderPrefs.isChecked = prefs.isBirthdayReminderEnabled
  }

  private fun changeBirthdayPrefs() {
    val isChecked = binding.birthReminderPrefs.isChecked
    binding.birthReminderPrefs.isChecked = !isChecked
    prefs.isBirthdayReminderEnabled = !isChecked
    if (!isChecked) {
      jobScheduler.scheduleDailyBirthday()
    } else {
      cleanBirthdays()
      jobScheduler.cancelDailyBirthday()
    }
  }

  private fun cleanBirthdays() {
    viewModel.deleteAllBirthdays()
  }

  override fun getTitle(): String = getString(R.string.birthdays)

  override fun onTimeSet(timePicker: TimePicker, i: Int, i1: Int) {
    prefs.birthdayTime = TimeUtil.getBirthdayTime(i, i1)
    initBirthdayTimePrefs()
    if (prefs.isBirthdayReminderEnabled) {
      jobScheduler.scheduleDailyBirthday()
    }
  }
}
