package com.elementary.tasks.navigation.settings.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsRemindersBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import java.util.*

class RemindersSettingsFragment : BaseSettingsFragment<FragmentSettingsRemindersBinding>() {

  private var mItemSelect: Int = 0

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsRemindersBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ViewUtils.listenScrollableView(binding.scrollView) {
      setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
    }

    initDefaultPriority()
    initCompletedPrefs()
    initDoNotDisturbPrefs()
    initTimesPrefs()
    initActionPrefs()
    initIgnorePrefs()
    initPermanentPrefs()
  }

  private fun initPermanentPrefs() {
    binding.permanentPrefs.setOnClickListener { changePermanent() }
    binding.permanentPrefs.isChecked = prefs.showPermanentOnHome
  }

  private fun changePermanent() {
    val isChecked = binding.permanentPrefs.isChecked
    binding.permanentPrefs.isChecked = !isChecked
    prefs.showPermanentOnHome = !isChecked
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
      showTimeDialog(prefs.doNotDisturbFrom) { i, j ->
        prefs.doNotDisturbFrom = TimeUtil.getBirthdayTime(i, j)
        showFromTime()
      }
    }
    binding.doNotDisturbFromPrefs.setDependentView(binding.doNotDisturbPrefs)

    binding.doNotDisturbToPrefs.setOnClickListener {
      showTimeDialog(prefs.doNotDisturbTo) { i, j ->
        prefs.doNotDisturbTo = TimeUtil.getBirthdayTime(i, j)
        showToTime()
      }
    }
    binding.doNotDisturbToPrefs.setDependentView(binding.doNotDisturbPrefs)

    showFromTime()
    showToTime()
  }

  private fun showTimeDialog(time: String, callback: (Int, Int) -> Unit) {
    val calendar = TimeUtil.getBirthdayCalendar(time)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val min = calendar.get(Calendar.MINUTE)
    withContext {
      TimeUtil.showTimePicker(it, prefs.is24HourFormat, hour, min
      ) { _, hourOfDay, minute ->
        callback.invoke(hourOfDay, minute)
      }
    }
  }

  private fun showToTime() {
    binding.doNotDisturbToPrefs.setValueText(TimeUtil.getBirthdayVisualTime(prefs.doNotDisturbTo, prefs.is24HourFormat, prefs.appLanguage))
  }

  private fun showFromTime() {
    binding.doNotDisturbFromPrefs.setValueText(TimeUtil.getBirthdayVisualTime(prefs.doNotDisturbFrom, prefs.is24HourFormat, prefs.appLanguage))
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
      builder.setTitle(getString(R.string.default_priority))
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

  override fun getTitle(): String = getString(R.string.reminders_)
}
