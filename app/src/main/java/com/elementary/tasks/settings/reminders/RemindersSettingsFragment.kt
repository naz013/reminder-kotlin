package com.elementary.tasks.settings.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.databinding.FragmentSettingsRemindersBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalTime

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
    initBuilderType()
    initPresets()
  }

  private fun initPresets() {
    binding.presetsPrefs.setOnClickListener {
      safeNavigation {
        RemindersSettingsFragmentDirections.actionRemindersSettingsFragmentToManagePresetsFragment()
      }
    }
  }

  private fun initBuilderType() {
    binding.defaultBuilderPrefs.visibleGone(prefs.canChangeBuilder || Module.isDebug)
    binding.defaultBuilderPrefs.setOnClickListener { showBuilderTypeDialog() }
    showBuilderType()
  }

  private fun showBuilderTypeDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(getString(R.string.reminder_builder))
      mItemSelect = if (prefs.useLegacyBuilder) {
        1
      } else {
        0
      }
      builder.setSingleChoiceItems(builderTypeList(), mItemSelect) { _, which ->
        mItemSelect = which
      }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        prefs.useLegacyBuilder = mItemSelect == 1
        showBuilderType()
        dialog.dismiss()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun builderTypeList(): Array<String> {
    return arrayOf(
      getString(R.string.default_string),
      getString(R.string.legacy)
    )
  }

  private fun showBuilderType() {
    val type = if (prefs.useLegacyBuilder) {
      getString(R.string.legacy)
    } else {
      getString(R.string.default_string)
    }
    binding.defaultBuilderPrefs.setDetailText(type)
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
