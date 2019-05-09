package com.elementary.tasks.navigation.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TimePicker
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.work.ScanContactsWorker
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.services.AlarmReceiver
import com.elementary.tasks.core.services.EventJobService
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.birthdays.BirthdaysViewModel
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentSettingsBirthdaysSettingsBinding
import java.util.*

class BirthdaySettingsFragment : BaseCalendarFragment<FragmentSettingsBirthdaysSettingsBinding>(), TimePickerDialog.OnTimeSetListener {

    private lateinit var viewModel: BirthdaysViewModel
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

    override fun layoutRes(): Int = R.layout.fragment_settings_birthdays_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        initBirthdayReminderPrefs()
        initBirthdaysWidgetPrefs()
        initPermanentPrefs()
        initDaysToPrefs()
        initBirthdayTimePrefs()
        initContactsPrefs()
        initContactsAutoPrefs()
        initNotificationPrefs()
        initViewModel()
        initScanButton()
        initPriority()
    }

    override fun onDestroy() {
        super.onDestroy()
        ScanContactsWorker.unsubscribe()
    }

    private fun initPriority() {
        binding.priorityPrefs.setOnClickListener { showPriorityDialog() }
        binding.priorityPrefs.setDependentView(binding.birthReminderPrefs)
        showPriority()
    }

    private fun showPriorityDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
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
    }

    private fun showPriority() {
        binding.priorityPrefs.setDetailText(priorityList()[prefs.birthdayPriority])
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(BirthdaysViewModel::class.java)
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> {
                    }
                    else -> {
                    }
                }
            }
        })
    }

    private fun initNotificationPrefs() {
        binding.birthdayNotificationPrefs.setOnClickListener {
            findNavController().navigate(BirthdaySettingsFragmentDirections.actionBirthdaySettingsFragmentToBirthdayNotificationFragment())
        }
        binding.birthdayNotificationPrefs.setDependentView(binding.birthReminderPrefs)
    }

    private fun initScanButton() {
        if (prefs.isContactBirthdaysEnabled) {
            binding.scanButton.isEnabled = true
            binding.scanButton.visibility = View.VISIBLE
            binding.scanButton.setOnClickListener { scanForBirthdays() }
            ScanContactsWorker.onEnd = {
                val message = if (it == 0) {
                    getString(R.string.no_new_birthdays)
                } else {
                    getString(R.string.found) + " $it " + getString(R.string.birthdays)
                }
                Toast.makeText(context!!, message, Toast.LENGTH_SHORT).show()
                onProgress.invoke(false)
            }
            ScanContactsWorker.listener = onProgress
        } else {
            binding.scanButton.visibility = View.GONE
        }
    }

    private fun scanForBirthdays() {
        withActivity {
            if (!Permissions.ensurePermissions(it, BIRTHDAYS_CODE, Permissions.READ_CONTACTS)) {
                return@withActivity
            }
            onProgress.invoke(true)
            ScanContactsWorker.scan(it)
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
            AlarmReceiver().enableBirthdayCheckAlarm()
        } else {
            AlarmReceiver().cancelBirthdayCheckAlarm()
        }
    }

    private fun initContactsPrefs() {
        binding.useContactsPrefs.isChecked = prefs.isContactBirthdaysEnabled
        binding.useContactsPrefs.setOnClickListener { changeContactsPrefs() }
        binding.useContactsPrefs.setDependentView(binding.birthReminderPrefs)
    }

    private fun changeContactsPrefs() {
        withActivity {
            if (!Permissions.ensurePermissions(it, CONTACTS_CODE, Permissions.READ_CONTACTS)) {
                return@withActivity
            }
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
        withContext {
            TimeUtil.showTimePicker(it, themeUtil.dialogStyle, prefs.is24HourFormat, hour, minute, this)
        }
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
        withContext {
            val isChecked = binding.birthdayPermanentPrefs.isChecked
            binding.birthdayPermanentPrefs.isChecked = !isChecked
            prefs.isBirthdayPermanentEnabled = !isChecked
            if (!isChecked) {
                it.sendBroadcast(Intent(it, PermanentBirthdayReceiver::class.java).setAction(PermanentBirthdayReceiver.ACTION_SHOW))
                AlarmReceiver().enableBirthdayPermanentAlarm(it)
            } else {
                it.sendBroadcast(Intent(it, PermanentBirthdayReceiver::class.java).setAction(PermanentBirthdayReceiver.ACTION_HIDE))
                AlarmReceiver().cancelBirthdayPermanentAlarm(it)
            }
        }
    }

    private fun initBirthdaysWidgetPrefs() {
        binding.widgetShowPrefs.isChecked = prefs.isBirthdayInWidgetEnabled
        binding.widgetShowPrefs.setOnClickListener { changeWidgetPrefs() }
        binding.widgetShowPrefs.setDependentView(binding.birthReminderPrefs)
    }

    private fun changeWidgetPrefs() {
        withContext {
            val isChecked = binding.widgetShowPrefs.isChecked
            binding.widgetShowPrefs.isChecked = !isChecked
            prefs.isBirthdayInWidgetEnabled = !isChecked
            UpdatesHelper.updateCalendarWidget(it)
            UpdatesHelper.updateWidget(it)
        }
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
            EventJobService.enableBirthdayAlarm(prefs)
        } else {
            cleanBirthdays()
            EventJobService.cancelBirthdayAlarm()
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
            EventJobService.enableBirthdayAlarm(prefs)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CONTACTS_CODE -> if (Permissions.isAllGranted(grantResults)) {
                changeContactsPrefs()
            }
            BIRTHDAYS_CODE -> if (Permissions.isAllGranted(grantResults)) {
                scanForBirthdays()
            }
        }
    }

    companion object {
        private const val CONTACTS_CODE = 302
        private const val BIRTHDAYS_CODE = 303
    }
}
