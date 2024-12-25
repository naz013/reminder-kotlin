package com.elementary.tasks.settings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.databinding.FragmentSettingsNotificationBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.github.naz013.common.Permissions
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.databinding.DialogWithSeekAndTitleBinding
import java.util.Locale

class NotificationSettingsFragment : BaseSettingsFragment<FragmentSettingsNotificationBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsNotificationBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initSbPrefs()
    initSbIconPrefs()
    initVibratePrefs()
    initInfiniteVibratePrefs()
    initSnoozeTimePrefs()
    initLedPrefs()
    initLedColorPrefs()
    initRepeatPrefs()
    initRepeatTimePrefs()
    initWearNotification()
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
      builder.setTitle(R.string.interval)
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
        title = getString(R.string.led_color),
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
      builder.setTitle(R.string.snooze_time)
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

  private fun changeInfiniteVibratePrefs() {
    val isChecked = binding.infiniteVibrateOptionPrefs.isChecked
    binding.infiniteVibrateOptionPrefs.isChecked = !isChecked
    prefs.isInfiniteVibrateEnabled = !isChecked
  }

  private fun initInfiniteVibratePrefs() {
    binding.infiniteVibrateOptionPrefs.setOnClickListener { changeInfiniteVibratePrefs() }
    binding.infiniteVibrateOptionPrefs.isChecked = prefs.isInfiniteVibrateEnabled
    binding.infiniteVibrateOptionPrefs.setDependentView(binding.vibrationOptionPrefs)
  }

  private fun changeVibratePrefs() {
    val isChecked = binding.vibrationOptionPrefs.isChecked
    binding.vibrationOptionPrefs.isChecked = !isChecked
    prefs.isVibrateEnabled = !isChecked
  }

  private fun initVibratePrefs() {
    binding.vibrationOptionPrefs.setOnClickListener { changeVibratePrefs() }
    binding.vibrationOptionPrefs.isChecked = prefs.isVibrateEnabled
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

  override fun getTitle() = getString(R.string.notification)
}
