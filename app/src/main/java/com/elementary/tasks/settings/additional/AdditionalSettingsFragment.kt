package com.elementary.tasks.settings.additional

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsAdditionalBinding
import com.elementary.tasks.settings.BaseSettingsFragment
import java.util.*

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
    withActivity {
      if (!Permissions.checkPermission(it, FOLLOW, Permissions.READ_PHONE_STATE)) {
        return@withActivity
      }
      val isChecked = binding.followReminderPrefs.isChecked
      binding.followReminderPrefs.isChecked = !isChecked
      prefs.isFollowReminderEnabled = !isChecked
    }
  }

  private fun changeMissedPrefs() {
    withActivity {
      if (!Permissions.checkPermission(it, MISSED, Permissions.READ_PHONE_STATE)) {
        return@withActivity
      }
      val isChecked = binding.missedPrefs.isChecked
      binding.missedPrefs.isChecked = !isChecked
      prefs.isMissedReminderEnabled = !isChecked
    }
  }

  private fun changeQuickSmsPrefs() {
    withActivity {
      if (!Permissions.checkPermission(it, QUICK_SMS, Permissions.READ_PHONE_STATE)) {
        return@withActivity
      }
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
      b.seekBar.max = 60
      b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
          b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes),
            progress.toString())
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
      })
      val time = prefs.missedReminderTime
      b.seekBar.progress = time
      b.titleView.text = String.format(Locale.getDefault(), getString(R.string.x_minutes), time.toString())
      builder.setView(b.root)
      builder.setPositiveButton(R.string.ok) { _, _ ->
        prefs.missedReminderTime = b.seekBar.progress
        showTime()
      }
      builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
      val dialog = builder.create()
      dialog.show()
      Dialogues.setFullWidthDialog(dialog, it)
    }
  }

  override fun getTitle(): String = getString(R.string.additional)

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (Permissions.checkPermission(grantResults)) {
      when (requestCode) {
        MISSED -> changeMissedPrefs()
        QUICK_SMS -> changeQuickSmsPrefs()
        FOLLOW -> changeFollowPrefs()
      }
    }
  }

  companion object {
    private const val MISSED = 107
    private const val QUICK_SMS = 108
    private const val FOLLOW = 109
  }
}
