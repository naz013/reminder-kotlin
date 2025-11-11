package com.elementary.tasks.settings.reminders

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.databinding.FragmentSettingsRemindersBinding
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.github.naz013.common.Module
import com.github.naz013.common.Permissions
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.databinding.DialogWithSeekAndTitleBinding
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visible
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalTime
import java.util.Locale

class RemindersSettingsFragment : BaseSettingsFragment<FragmentSettingsRemindersBinding>() {

  private val dateTimeManager by inject<DateTimeManager>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()

  private var mItemSelect: Int = 0

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsRemindersBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initDefaultPriority()
    initCompletedPrefs()
    initDoNotDisturbPrefs()
    initTimesPrefs()
    initActionPrefs()
    initIgnorePrefs()
    initPresets()
    initLocationPrefs()

    initSnoozeTimePrefs()
    initLedPrefs()
    initLedColorPrefs()
    initRepeatPrefs()
    initRepeatTimePrefs()

    initWearNotification()

    initSbPrefs()
    initSbIconPrefs()
  }

  private fun initSbIconPrefs() {
    binding.statusIconPrefs.setOnClickListener { changeSbIconPrefs() }
    binding.statusIconPrefs.isChecked = prefs.isSbIconEnabled
    binding.statusIconPrefs.setDependentView(binding.permanentNotificationPrefs)
  }

  private fun changeSbIconPrefs() {
    val isChecked = binding.statusIconPrefs.isChecked
    binding.statusIconPrefs.isChecked = !isChecked
    prefs.isSbIconEnabled = !isChecked
    PermanentReminderReceiver.show(requireContext())
  }

  private fun initSbPrefs() {
    binding.permanentNotificationPrefs.setOnClickListener { tryChangeSbPrefs() }
    binding.permanentNotificationPrefs.isChecked = prefs.isSbNotificationEnabled
  }

  private fun tryChangeSbPrefs() {
    val isChecked = binding.permanentNotificationPrefs.isChecked
    Logger.d("tryChangeSbPrefs: $isChecked")
    if (!isChecked) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionFlow.askPermission(Permissions.POST_NOTIFICATION) { changeSbPrefs() }
      } else {
        changeSbPrefs()
      }
    } else {
      changeSbPrefs()
    }
  }

  private fun changeSbPrefs() {
    val isChecked = binding.permanentNotificationPrefs.isChecked
    binding.permanentNotificationPrefs.isChecked = !isChecked
    prefs.isSbNotificationEnabled = !isChecked
    if (prefs.isSbNotificationEnabled) {
      PermanentReminderReceiver.show(requireContext())
    } else {
      PermanentReminderReceiver.hide(requireContext())
    }
  }

  private fun initWearNotification() {
    binding.wearPrefs.isChecked = prefs.isWearEnabled
    binding.wearPrefs.setOnClickListener { changeWearNotification() }
  }

  private fun changeWearNotification() {
    val isChecked = binding.wearPrefs.isChecked
    prefs.isWearEnabled = !isChecked
    binding.wearPrefs.isChecked = !isChecked
  }

  private fun showRepeatTimeDialog() {
    withActivity {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(R.string.reminder_notification_repeat_interval)
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
      b.seekBar.valueTo = 60f

      val repeatTime = prefs.notificationRepeatTime
      b.seekBar.value = repeatTime.toFloat()

      b.titleView.text = String.format(
        Locale.getDefault(),
        getString(R.string.x_minutes),
        repeatTime.toString()
      )
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ ->
        prefs.notificationRepeatTime = b.seekBar.value.toInt()
        showRepeatTime()
        initRepeatTimePrefs()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
      val dialog = builder.create()
      dialog.show()
      Dialogues.setFullWidthDialog(dialog, it)
    }
  }

  private fun initRepeatTimePrefs() {
    binding.repeatIntervalPrefs.setValue(prefs.notificationRepeatTime)
    binding.repeatIntervalPrefs.setOnClickListener { showRepeatTimeDialog() }
    binding.repeatIntervalPrefs.setDependentView(binding.repeatNotificationOptionPrefs)
    showRepeatTime()
  }

  private fun showRepeatTime() {
    binding.repeatIntervalPrefs.setDetailText(
      String.format(
        Locale.getDefault(),
        getString(R.string.x_minutes),
        prefs.notificationRepeatTime.toString()
      )
    )
  }

  private fun changeRepeatPrefs() {
    val isChecked = binding.repeatNotificationOptionPrefs.isChecked
    binding.repeatNotificationOptionPrefs.isChecked = !isChecked
    prefs.isNotificationRepeatEnabled = !isChecked
  }

  private fun initRepeatPrefs() {
    binding.repeatNotificationOptionPrefs.setOnClickListener { changeRepeatPrefs() }
    binding.repeatNotificationOptionPrefs.isChecked = prefs.isNotificationRepeatEnabled
  }

  private fun showLedColorDialog() {
    dialogues.showPropertyDialog(
      requireContext(),
      Dialogues.SelectionList(
        position = prefs.ledColor,
        title = getString(R.string.led_indication_color),
        okButtonTitle = getString(R.string.ok),
        cancelButtonTitle = getString(R.string.cancel),
        items = LED.getAllNames(requireContext())
      ),
      onOk = {
        prefs.ledColor = it
        showLedColor()
      }
    )
  }

  private fun showLedColor() {
    binding.chooseLedColorPrefs.setDetailText(LED.getTitle(requireContext(), prefs.ledColor))
  }

  private fun initLedColorPrefs() {
    binding.chooseLedColorPrefs.setOnClickListener { showLedColorDialog() }
    binding.chooseLedColorPrefs.setDependentView(binding.ledPrefs)
    showLedColor()
  }

  private fun changeLedPrefs() {
    val isChecked = binding.ledPrefs.isChecked
    binding.ledPrefs.isChecked = !isChecked
    prefs.isLedEnabled = !isChecked
  }

  private fun initLedPrefs() {
    binding.ledPrefs.setOnClickListener { changeLedPrefs() }
    binding.ledPrefs.isChecked = prefs.isLedEnabled
  }

  private fun initSnoozeTimePrefs() {
    binding.delayForPrefs.setOnClickListener { showSnoozeDialog() }
    binding.delayForPrefs.setValue(prefs.snoozeTime)
    showSnooze()
  }

  private fun showSnooze() {
    binding.delayForPrefs.setDetailText(
      String.format(
        Locale.getDefault(),
        getString(R.string.x_minutes),
        prefs.snoozeTime.toString()
      )
    )
  }

  private fun snoozeFormat(progress: Int): String {
    if (!isAdded) {
      return ""
    }
    return String.format(Locale.getDefault(), getString(R.string.x_minutes), progress.toString())
  }

  private fun showSnoozeDialog() {
    dialogues.getNullableDialog(context)?.let { builder ->
      builder.setTitle(R.string.default_reminder_snooze_time)
      val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)

      b.seekBar.addOnChangeListener { _, value, _ ->
        b.titleView.text = snoozeFormat(value.toInt())
      }
      b.seekBar.stepSize = 1f
      b.seekBar.valueFrom = 0f
      b.seekBar.valueTo = 60f

      val snoozeTime = prefs.snoozeTime
      b.seekBar.value = snoozeTime.toFloat()

      b.titleView.text = snoozeFormat(snoozeTime)
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ ->
        prefs.snoozeTime = b.seekBar.value.toInt()
        showSnooze()
        initSnoozeTimePrefs()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
      val dialog = builder.create()
      dialog.show()
      Dialogues.setFullWidthDialog(dialog, activity)
    }
  }

  private fun initLocationPrefs() {
    if (Module.hasLocation(requireContext())) {
      binding.locationSettings.setOnClickListener { openLocationSettingsScreen() }
      binding.locationSettings.visible()
    } else {
      binding.locationSettings.gone()
    }
  }

  private fun openLocationSettingsScreen() {
    navigate {
      navigate(
        R.id.locationSettingsFragment,
        null,
        NavigationAnimations.inDepthNavOptions()
      )
    }
  }

  private fun initPresets() {
    binding.presetsPrefs.setOnClickListener {
      safeNavigation {
        RemindersSettingsFragmentDirections.actionRemindersSettingsFragmentToManagePresetsFragment()
      }
    }
  }

  private fun initIgnorePrefs() {
    binding.doNotDisturbIgnorePrefs.setOnClickListener { showIgnoreDialog() }
    binding.doNotDisturbIgnorePrefs.setDependentView(binding.doNotDisturbPrefs)
    showIgnore()
  }

  private fun showIgnore() {
    binding.doNotDisturbIgnorePrefs.setDetailText(ignoreList()[prefs.doNotDisturbIgnore])
  }

  private fun showIgnoreDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(getString(R.string.priority))
      mItemSelect = prefs.doNotDisturbIgnore
      builder.setSingleChoiceItems(ignoreList(), mItemSelect) { _, which ->
        mItemSelect = which
      }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        prefs.doNotDisturbIgnore = mItemSelect
        showIgnore()
        dialog.dismiss()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun initActionPrefs() {
    binding.doNotDisturbActionPrefs.setOnClickListener { showActionDialog() }
    binding.doNotDisturbActionPrefs.setDependentView(binding.doNotDisturbPrefs)
    showAction()
  }

  private fun showAction() {
    binding.doNotDisturbActionPrefs.setDetailText(actionList()[prefs.doNotDisturbAction])
  }

  private fun showActionDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(getString(R.string.events_that_occured_during))
      mItemSelect = prefs.doNotDisturbAction
      builder.setSingleChoiceItems(actionList(), mItemSelect) { _, which ->
        mItemSelect = which
      }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        prefs.doNotDisturbAction = mItemSelect
        showAction()
        dialog.dismiss()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun initTimesPrefs() {
    binding.doNotDisturbFromPrefs.setOnClickListener {
      val time = dateTimeManager.toLocalTime(prefs.doNotDisturbFrom) ?: LocalTime.now()
      dateTimePickerProvider.showTimePicker(
        fragmentManager = childFragmentManager,
        time = time,
        title = getString(R.string.from)
      ) {
        prefs.doNotDisturbFrom = dateTimeManager.to24HourString(it)
        showFromTime()
      }
    }
    binding.doNotDisturbFromPrefs.setDependentView(binding.doNotDisturbPrefs)

    binding.doNotDisturbToPrefs.setOnClickListener {
      val time = dateTimeManager.toLocalTime(prefs.doNotDisturbTo) ?: LocalTime.now()
      dateTimePickerProvider.showTimePicker(
        fragmentManager = childFragmentManager,
        time = time,
        title = getString(R.string.to)
      ) {
        prefs.doNotDisturbTo = dateTimeManager.to24HourString(it)
        showToTime()
      }
    }
    binding.doNotDisturbToPrefs.setDependentView(binding.doNotDisturbPrefs)

    showFromTime()
    showToTime()
  }

  private fun showToTime() {
    binding.doNotDisturbToPrefs.setValueText(
      dateTimeManager.getTime(dateTimeManager.toLocalTime(prefs.doNotDisturbTo) ?: LocalTime.now())
    )
  }

  private fun showFromTime() {
    binding.doNotDisturbFromPrefs.setValueText(
      dateTimeManager.getTime(
        dateTimeManager.toLocalTime(prefs.doNotDisturbFrom) ?: LocalTime.now()
      )
    )
  }

  private fun initDoNotDisturbPrefs() {
    binding.doNotDisturbPrefs.setOnClickListener { changeDoNotDisturb() }
    binding.doNotDisturbPrefs.isChecked = prefs.isDoNotDisturbEnabled
  }

  private fun changeDoNotDisturb() {
    val isChecked = binding.doNotDisturbPrefs.isChecked
    binding.doNotDisturbPrefs.isChecked = !isChecked
    prefs.isDoNotDisturbEnabled = !isChecked
  }

  private fun initDefaultPriority() {
    binding.defaultPriorityPrefs.setOnClickListener { showPriorityDialog() }
    showDefaultPriority()
  }

  private fun showPriorityDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(getString(R.string.reminder_default_priority))
      mItemSelect = prefs.defaultPriority
      builder.setSingleChoiceItems(priorityList(), mItemSelect) { _, which ->
        mItemSelect = which
      }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        prefs.defaultPriority = mItemSelect
        showDefaultPriority()
        dialog.dismiss()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun showDefaultPriority() {
    binding.defaultPriorityPrefs.setDetailText(priorityList()[prefs.defaultPriority])
  }

  private fun initCompletedPrefs() {
    binding.completedPrefs.setOnClickListener { changeCompleted() }
    binding.completedPrefs.isChecked = prefs.moveCompleted
  }

  private fun changeCompleted() {
    val isChecked = binding.completedPrefs.isChecked
    binding.completedPrefs.isChecked = !isChecked
    prefs.moveCompleted = !isChecked
  }

  private fun ignoreList(): Array<String> {
    return arrayOf(
      getString(R.string.priority_lowest) + " " + getString(R.string.and_above),
      getString(R.string.priority_low) + " " + getString(R.string.and_above),
      getString(R.string.priority_normal) + " " + getString(R.string.and_above),
      getString(R.string.priority_high) + " " + getString(R.string.and_above),
      getString(R.string.priority_highest),
      getString(R.string.do_not_allow)
    )
  }

  private fun actionList(): Array<String> {
    return arrayOf(
      getString(R.string.schedule_for_later),
      getString(R.string.ignore)
    )
  }

  override fun getTitle(): String {
    return arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) ?: getString(R.string.reminders_)
  }

  override fun getNavigationIcon(): Int {
    return if (arguments?.getString(IntentKeys.INTENT_SCREEN_TITLE) == null) {
      super.getNavigationIcon()
    } else {
      R.drawable.ic_builder_clear
    }
  }
}
