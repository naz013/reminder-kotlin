package com.elementary.tasks.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.databinding.FragmentSettingsBirthdaysBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.elementary.tasks.settings.birthday.BirthdaySettingsViewModel
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.Permissions
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.databinding.DialogWithSeekAndTitleBinding
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.view.transparent
import com.github.naz013.ui.common.view.visible
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalTime

class BirthdaySettingsFragment : BaseSettingsFragment<FragmentSettingsBirthdaysBinding>() {

  private val viewModel by viewModel<BirthdaySettingsViewModel>()
  private val jobScheduler by inject<JobScheduler>()
  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val dateTimeManager by inject<DateTimeManager>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()

  private var mItemSelect: Int = 0

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsBirthdaysBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initBirthdayReminderPrefs()
    initBirthdaysWidgetPrefs()
    initPermanentPrefs()
    initDaysToPrefs()
    initHomeDaysPrefs()
    initBirthdayTimePrefs()
    initContactsPrefs()
    initContactsAutoPrefs()
    initViewModel()
    initScanButton()
    initPriority()

    initGlobalPrefs()
    initVibratePrefs()
    initInfiniteVibratePrefs()
    initLedPrefs()
    initLedColorPrefs()
  }

  private fun initLedColorPrefs() {
    binding.chooseLedColorPrefs.setReverseDependentView(binding.globalOptionPrefs)
    binding.chooseLedColorPrefs.setDependentView(binding.ledPrefs)
    binding.chooseLedColorPrefs.setDependentView(binding.birthReminderPrefs)
    binding.chooseLedColorPrefs.setOnClickListener { showLedColorDialog() }
    showLedColor()
  }

  private fun showLedColor() {
    withContext {
      binding.chooseLedColorPrefs.setDetailText(LED.getTitle(it, prefs.birthdayLedColor))
    }
  }

  private fun showLedColorDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(getString(R.string.led_color))
      val colors = LED.getAllNames(it).toTypedArray()
      mItemSelect = prefs.birthdayLedColor
      builder.setSingleChoiceItems(colors, mItemSelect) { _, which -> mItemSelect = which }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        prefs.birthdayLedColor = mItemSelect
        showLedColor()
        dialog.dismiss()
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun initLedPrefs() {
    binding.ledPrefs.isChecked = prefs.isBirthdayLedEnabled
    binding.ledPrefs.setOnClickListener { changeLedPrefs() }
    binding.ledPrefs.setReverseDependentView(binding.globalOptionPrefs)
    binding.ledPrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun changeLedPrefs() {
    val isChecked = binding.ledPrefs.isChecked
    binding.ledPrefs.isChecked = !isChecked
    prefs.isBirthdayLedEnabled = !isChecked
  }

  private fun initInfiniteVibratePrefs() {
    binding.infiniteVibrateOptionPrefs.isChecked = prefs.isBirthdayInfiniteVibrationEnabled
    binding.infiniteVibrateOptionPrefs.setOnClickListener { changeInfiniteVibrationPrefs() }
    binding.infiniteVibrateOptionPrefs.setReverseDependentView(binding.globalOptionPrefs)
    binding.infiniteVibrateOptionPrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun changeInfiniteVibrationPrefs() {
    val isChecked = binding.infiniteVibrateOptionPrefs.isChecked
    binding.infiniteVibrateOptionPrefs.isChecked = !isChecked
    prefs.isBirthdayInfiniteVibrationEnabled = !isChecked
  }

  private fun initVibratePrefs() {
    binding.vibrationOptionPrefs.isChecked = prefs.isBirthdayVibrationEnabled
    binding.vibrationOptionPrefs.setOnClickListener { changeVibrationPrefs() }
    binding.vibrationOptionPrefs.setReverseDependentView(binding.globalOptionPrefs)
    binding.vibrationOptionPrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun changeVibrationPrefs() {
    val isChecked = binding.vibrationOptionPrefs.isChecked
    binding.vibrationOptionPrefs.isChecked = !isChecked
    prefs.isBirthdayVibrationEnabled = !isChecked
  }

  private fun initGlobalPrefs() {
    binding.globalOptionPrefs.isChecked = prefs.isBirthdayGlobalEnabled
    binding.globalOptionPrefs.setDependentView(binding.birthReminderPrefs)
    binding.globalOptionPrefs.setOnClickListener { changeGlobalPrefs() }
  }

  private fun changeGlobalPrefs() {
    val isChecked = binding.globalOptionPrefs.isChecked
    binding.globalOptionPrefs.isChecked = !isChecked
    prefs.isBirthdayGlobalEnabled = !isChecked
  }

  private fun initViewModel() {
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) { commands ->
      when (commands) {
        Commands.DELETED -> {
        }

        else -> {
        }
      }
    }
    viewModel.isInProgress.nonNullObserve(viewLifecycleOwner) {
      if (it) {
        binding.progressMessageView.text = getString(R.string.please_wait)
        binding.scanButton.isEnabled = false
        binding.progressView.visible()
      } else {
        binding.progressView.transparent()
        binding.scanButton.isEnabled = true
      }
    }
    viewModel.errorEvent.observeEvent(viewLifecycleOwner) { toast(it) }
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

  private fun initScanButton() {
    if (prefs.isContactBirthdaysEnabled) {
      binding.scanButton.isEnabled = true
      binding.scanButton.visibility = View.VISIBLE
      binding.scanButton.setOnClickListener { scanForBirthdays() }
    } else {
      binding.scanButton.visibility = View.GONE
    }
  }

  private fun scanForBirthdays() {
    permissionFlow.askPermission(Permissions.READ_CONTACTS) {
      viewModel.startScan()
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
    binding.reminderTimePrefs.setValueText(dateTimeManager.getBirthdayVisualTime())
    binding.reminderTimePrefs.setDependentView(binding.birthReminderPrefs)
  }

  private fun showTimeDialog() {
    val time = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
    dateTimePickerProvider.showTimePicker(
      fragmentManager = childFragmentManager,
      time = time,
      title = getString(R.string.remind_at)
    ) {
      prefs.birthdayTime = dateTimeManager.to24HourString(it)
      initBirthdayTimePrefs()
      if (prefs.isBirthdayReminderEnabled) {
        jobScheduler.scheduleDailyBirthday()
      }
    }
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

      val days = prefs.birthdayDurationInDays

      b.seekBar.addOnChangeListener { _, value, _ ->
        b.titleView.text = homeText(value.toInt())
      }
      b.seekBar.stepSize = 1f
      b.seekBar.valueFrom = 0f
      b.seekBar.valueTo = 5f
      b.seekBar.value = days.toFloat()

      b.titleView.text = homeText(days)
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ -> saveHomeDays(b.seekBar.value.toInt()) }
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

      val daysToBirthday = prefs.daysToBirthday

      b.seekBar.addOnChangeListener { _, value, _ ->
        b.titleView.text = value.toInt().toString()
      }
      b.seekBar.stepSize = 1f
      b.seekBar.valueFrom = 0f
      b.seekBar.valueTo = 5f
      b.seekBar.value = daysToBirthday.toFloat()

      b.titleView.text = daysToBirthday.toString()
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ -> saveDays(b.seekBar.value.toInt()) }
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
      requireActivity().sendBroadcast(
        Intent(requireContext(), PermanentBirthdayReceiver::class.java)
          .setAction(PermanentBirthdayReceiver.ACTION_SHOW)
      )
      jobScheduler.scheduleBirthdayPermanent()
    } else {
      requireActivity().sendBroadcast(
        Intent(requireContext(), PermanentBirthdayReceiver::class.java)
          .setAction(PermanentBirthdayReceiver.ACTION_HIDE)
      )
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
    appWidgetUpdater.updateCalendarWidget()
    appWidgetUpdater.updateAllWidgets()
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

  override fun getTitle(): String {
    return arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) ?: getString(R.string.birthdays)
  }

  override fun getNavigationIcon(): Int {
    return if (arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) == null) {
      super.getNavigationIcon()
    } else {
      R.drawable.ic_builder_clear
    }
  }
}
