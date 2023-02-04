package com.elementary.tasks.settings.additional

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsAdditionalBinding
import com.elementary.tasks.settings.BaseSettingsFragment
import java.util.Locale

class AdditionalSettingsFragment : BaseSettingsFragment<FragmentSettingsAdditionalBinding>() {

  private var mItemSelect: Int = 0

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsAdditionalBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ViewUtils.listenScrollableView(binding.scrollView) {
      setToolbarAlpha(toAlpha(it.toFloat(), NESTED_SCROLL_MAX))
    }

    initMissedPrefs()
    initMissedTimePrefs()
    initQuickSmsPrefs()
    initMessagesPrefs()
    binding.followReminderPrefs.setOnClickListener { changeFollowPrefs() }
    binding.followReminderPrefs.isChecked = prefs.isFollowReminderEnabled
    initPriority()
  }

  private fun initPriority() {
    binding.priorityPrefs.setOnClickListener { showPriorityDialog() }
    binding.priorityPrefs.setDependentView(binding.missedPrefs)
    showPriority()
  }

  private fun showPriorityDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(getString(R.string.default_priority))
      mItemSelect = prefs.missedCallPriority
      builder.setSingleChoiceItems(priorityList(), mItemSelect) { _, which ->
        mItemSelect = which
      }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        prefs.missedCallPriority = mItemSelect
        showPriority()
        dialog.dismiss()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun showPriority() {
    binding.priorityPrefs.setDetailText(priorityList()[prefs.missedCallPriority])
  }

  private fun initMessagesPrefs() {
    val mMessagesPrefs = binding.templatesPrefs
    mMessagesPrefs.setOnClickListener {
      safeNavigation(AdditionalSettingsFragmentDirections.actionAdditionalSettingsFragmentToTemplatesFragment())
    }
    mMessagesPrefs.setDependentView(binding.quickSMSPrefs)
  }

  private fun initQuickSmsPrefs() {
    binding.quickSMSPrefs.setOnClickListener { changeQuickSmsPrefs() }
    binding.quickSMSPrefs.isChecked = prefs.isQuickSmsEnabled
  }

  private fun initMissedTimePrefs() {
    binding.missedTimePrefs.setOnClickListener { showTimePickerDialog() }
    binding.missedTimePrefs.setDependentView(binding.missedPrefs)
    showTime()
  }

  private fun showTime() {
    binding.missedTimePrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
      prefs.missedReminderTime.toString()))
  }

  private fun initMissedPrefs() {
    binding.missedPrefs.setOnClickListener { changeMissedPrefs() }
    binding.missedPrefs.isChecked = prefs.isMissedReminderEnabled
  }

  private fun changeFollowPrefs() {
    permissionFlow.askPermission(Permissions.READ_PHONE_STATE) {
      val isChecked = binding.followReminderPrefs.isChecked
      binding.followReminderPrefs.isChecked = !isChecked
      prefs.isFollowReminderEnabled = !isChecked
    }
  }

  private fun changeMissedPrefs() {
    permissionFlow.askPermissions(listOf(Permissions.READ_PHONE_STATE, Permissions.POST_NOTIFICATION)) {
      val isChecked = binding.missedPrefs.isChecked
      binding.missedPrefs.isChecked = !isChecked
      prefs.isMissedReminderEnabled = !isChecked
    }
  }

  private fun changeQuickSmsPrefs() {
    permissionFlow.askPermission(Permissions.READ_PHONE_STATE) {
      val isChecked = binding.quickSMSPrefs.isChecked
      binding.quickSMSPrefs.isChecked = !isChecked
      prefs.isQuickSmsEnabled = !isChecked
    }
  }

  private fun showTimePickerDialog() {
    withActivity {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(R.string.interval)
      val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context))

      b.seekBar.addOnChangeListener { _, value, _ ->
        b.titleView.text = String.format(
          Locale.getDefault(), getString(R.string.x_minutes),
          value.toInt().toString()
        )
      }
      b.seekBar.stepSize = 1f
      b.seekBar.valueFrom = 0f
      b.seekBar.valueTo = 60f

      val time = prefs.missedReminderTime
      b.seekBar.value = time.toFloat()

      b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes), time.toString())
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ ->
        prefs.missedReminderTime = b.seekBar.value.toInt()
        showTime()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
      val dialog = builder.create()
      dialog.show()
      Dialogues.setFullWidthDialog(dialog, it)
    }
  }

  override fun getTitle(): String = getString(R.string.additional)
}
