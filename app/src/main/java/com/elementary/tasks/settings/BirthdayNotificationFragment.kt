package com.elementary.tasks.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.databinding.FragmentSettingsBirthdayNotificationsBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment

class BirthdayNotificationFragment :
  BaseSettingsFragment<FragmentSettingsBirthdayNotificationsBinding>() {

  private var mItemSelect: Int = 0

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsBirthdayNotificationsBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initGlobalPrefs()
    initVibratePrefs()
    initInfiniteVibratePrefs()
    initLedPrefs()
    initLedColorPrefs()
  }

  private fun initLedColorPrefs() {
    binding.chooseLedColorPrefs.setReverseDependentView(binding.globalOptionPrefs)
    binding.chooseLedColorPrefs.setDependentView(binding.ledPrefs)
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
  }

  private fun changeVibrationPrefs() {
    val isChecked = binding.vibrationOptionPrefs.isChecked
    binding.vibrationOptionPrefs.isChecked = !isChecked
    prefs.isBirthdayVibrationEnabled = !isChecked
  }

  private fun initGlobalPrefs() {
    binding.globalOptionPrefs.isChecked = prefs.isBirthdayGlobalEnabled
    binding.globalOptionPrefs.setOnClickListener { changeGlobalPrefs() }
  }

  private fun changeGlobalPrefs() {
    val isChecked = binding.globalOptionPrefs.isChecked
    binding.globalOptionPrefs.isChecked = !isChecked
    prefs.isBirthdayGlobalEnabled = !isChecked
  }

  override fun getTitle(): String = getString(R.string.birthday_notification)
}
