package com.elementary.tasks.navigation.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TimePicker

import com.elementary.tasks.R
import com.elementary.tasks.birthdays.work.CheckBirthdaysAsync
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.services.AlarmReceiver
import com.elementary.tasks.core.services.EventJobService
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.birthdays.BirthdaysViewModel
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding
import com.elementary.tasks.databinding.FragmentBirthdaysSettingsBinding

import java.util.Calendar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class BirthdaySettingsFragment : BaseSettingsFragment(), TimePickerDialog.OnTimeSetListener {

    private var binding: FragmentBirthdaysSettingsBinding? = null
    private var viewModel: BirthdaysViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBirthdaysSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBirthdayReminderPrefs()
        initBirthdaysWidgetPrefs()
        initPermanentPrefs()
        initDaysToPrefs()
        initBirthdayTimePrefs()
        initContactsPrefs()
        initContactsAutoPrefs()
        initScanPrefs()
        initNotificationPrefs()

        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(BirthdaysViewModel::class.java)
        viewModel!!.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> {
                    }
                }
            }
        })
    }

    private fun initNotificationPrefs() {
        binding!!.birthdayNotificationPrefs.setOnClickListener { view -> replaceFragment(BirthdayNotificationFragment(), getString(R.string.birthday_notification)) }
        binding!!.birthdayNotificationPrefs.setDependentView(binding!!.birthReminderPrefs)
    }

    private fun initScanPrefs() {
        binding!!.contactsScanPrefs.setDependentView(binding!!.useContactsPrefs)
        binding!!.contactsScanPrefs.setOnClickListener { view -> scanForBirthdays() }
        binding!!.contactsScanPrefs.setDependentView(binding!!.birthReminderPrefs)
    }

    private fun scanForBirthdays() {
        if (!Permissions.checkPermission(activity, Permissions.READ_CONTACTS)) {
            Permissions.requestPermission(activity, BIRTHDAYS_CODE, Permissions.READ_CONTACTS)
            return
        }
        CheckBirthdaysAsync(activity, true).execute()
    }

    private fun initContactsAutoPrefs() {
        binding!!.autoScanPrefs.isChecked = prefs!!.isContactAutoCheckEnabled
        binding!!.autoScanPrefs.setOnClickListener { view -> changeAutoPrefs() }
        binding!!.autoScanPrefs.setDependentView(binding!!.useContactsPrefs)
        binding!!.autoScanPrefs.setDependentView(binding!!.birthReminderPrefs)
    }

    private fun changeAutoPrefs() {
        val isChecked = binding!!.autoScanPrefs.isChecked
        binding!!.autoScanPrefs.isChecked = !isChecked
        prefs!!.isContactAutoCheckEnabled = !isChecked
        if (!isChecked) {
            AlarmReceiver().enableBirthdayCheckAlarm(context)
        } else {
            AlarmReceiver().cancelBirthdayCheckAlarm(context)
        }
    }

    private fun initContactsPrefs() {
        binding!!.useContactsPrefs.isChecked = prefs!!.isContactBirthdaysEnabled
        binding!!.useContactsPrefs.setOnClickListener { view -> changeContactsPrefs() }
        binding!!.useContactsPrefs.setDependentView(binding!!.birthReminderPrefs)
    }

    private fun changeContactsPrefs() {
        if (!Permissions.checkPermission(activity, Permissions.READ_CONTACTS)) {
            Permissions.requestPermission(activity, CONTACTS_CODE, Permissions.READ_CONTACTS)
            return
        }
        val isChecked = binding!!.useContactsPrefs.isChecked
        binding!!.useContactsPrefs.isChecked = !isChecked
        prefs!!.isContactBirthdaysEnabled = !isChecked
    }

    private fun initBirthdayTimePrefs() {
        binding!!.reminderTimePrefs.setOnClickListener { view -> showTimeDialog() }
        binding!!.reminderTimePrefs.setValueText(prefs!!.birthdayTime)
        binding!!.reminderTimePrefs.setDependentView(binding!!.birthReminderPrefs)
    }

    private fun showTimeDialog() {
        val calendar = TimeUtil.getBirthdayCalendar(prefs!!.birthdayTime)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimeUtil.showTimePicker(context, this, hour, minute)
    }

    private fun initDaysToPrefs() {
        binding!!.daysToPrefs.setOnClickListener { view -> showDaysToDialog() }
        binding!!.daysToPrefs.setValue(prefs!!.daysToBirthday)
        binding!!.daysToPrefs.setDependentView(binding!!.birthReminderPrefs)
    }

    private fun showDaysToDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setTitle(R.string.days_to_birthday)
        val b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(context))
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
        val daysToBirthday = prefs!!.daysToBirthday
        b.seekBar.progress = daysToBirthday
        b.titleView.text = daysToBirthday.toString()
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialog, which -> saveDays(b.seekBar.progress) }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun saveDays(progress: Int) {
        prefs!!.daysToBirthday = progress
        initDaysToPrefs()
    }

    private fun initPermanentPrefs() {
        binding!!.birthdayPermanentPrefs.isChecked = prefs!!.isBirthdayPermanentEnabled
        binding!!.birthdayPermanentPrefs.setOnClickListener { view -> changeBirthdayPermanentPrefs() }
        binding!!.birthdayPermanentPrefs.setDependentView(binding!!.birthReminderPrefs)
    }

    private fun changeBirthdayPermanentPrefs() {
        if (context == null) return
        val isChecked = binding!!.birthdayPermanentPrefs.isChecked
        binding!!.birthdayPermanentPrefs.isChecked = !isChecked
        prefs!!.isBirthdayPermanentEnabled = !isChecked
        if (!isChecked) {
            context!!.sendBroadcast(Intent(context, PermanentBirthdayReceiver::class.java).setAction(PermanentBirthdayReceiver.ACTION_SHOW))
            AlarmReceiver().enableBirthdayPermanentAlarm(context)
        } else {
            context!!.sendBroadcast(Intent(context, PermanentBirthdayReceiver::class.java).setAction(PermanentBirthdayReceiver.ACTION_HIDE))
            AlarmReceiver().cancelBirthdayPermanentAlarm(context)
        }
    }

    private fun initBirthdaysWidgetPrefs() {
        binding!!.widgetShowPrefs.isChecked = prefs!!.isBirthdayInWidgetEnabled
        binding!!.widgetShowPrefs.setOnClickListener { view -> changeWidgetPrefs() }
        binding!!.widgetShowPrefs.setDependentView(binding!!.birthReminderPrefs)
    }

    private fun changeWidgetPrefs() {
        val isChecked = binding!!.widgetShowPrefs.isChecked
        binding!!.widgetShowPrefs.isChecked = !isChecked
        prefs!!.isBirthdayInWidgetEnabled = !isChecked
        UpdatesHelper.getInstance(context).updateCalendarWidget()
        UpdatesHelper.getInstance(context).updateWidget()
    }

    private fun initBirthdayReminderPrefs() {
        binding!!.birthReminderPrefs.setOnClickListener { view -> changeBirthdayPrefs() }
        binding!!.birthReminderPrefs.isChecked = prefs!!.isBirthdayReminderEnabled
    }

    private fun changeBirthdayPrefs() {
        val isChecked = binding!!.birthReminderPrefs.isChecked
        binding!!.birthReminderPrefs.isChecked = !isChecked
        prefs!!.isBirthdayReminderEnabled = !isChecked
        if (!isChecked) {
            EventJobService.enableBirthdayAlarm(context)
        } else {
            cleanBirthdays()
            EventJobService.cancelBirthdayAlarm()
        }
    }

    private fun cleanBirthdays() {
        viewModel!!.deleteAllBirthdays()
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.birthdays))
            callback!!.onFragmentSelect(this)
        }
    }

    override fun onTimeSet(timePicker: TimePicker, i: Int, i1: Int) {
        prefs!!.birthdayTime = TimeUtil.getBirthdayTime(i, i1)
        initBirthdayTimePrefs()
        if (prefs!!.isBirthdayReminderEnabled) {
            EventJobService.enableBirthdayAlarm(context)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            CONTACTS_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                changeContactsPrefs()
            }
            BIRTHDAYS_CODE -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanForBirthdays()
            }
        }
    }

    companion object {

        private val CONTACTS_CODE = 302
        private val BIRTHDAYS_CODE = 303
    }
}
