package com.elementary.tasks.navigation.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TimePicker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.work.CheckBirthdaysWorker
import com.elementary.tasks.core.services.AlarmReceiver
import com.elementary.tasks.core.services.EventJobService
import com.elementary.tasks.core.services.PermanentBirthdayReceiver
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.birthdays.BirthdaysViewModel
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*
import kotlinx.android.synthetic.main.fragment_settings_birthdays_settings.*
import java.util.*

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
class BirthdaySettingsFragment : BaseCalendarFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var viewModel: BirthdaysViewModel

    override fun layoutRes(): Int = R.layout.fragment_settings_birthdays_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            callback?.onScrollUpdate(it)
        }

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
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> {
                    }
                }
            }
        })
    }

    private fun initNotificationPrefs() {
        birthdayNotificationPrefs.setOnClickListener { callback?.openFragment(BirthdayNotificationFragment(), getString(R.string.birthday_notification)) }
        birthdayNotificationPrefs.setDependentView(birthReminderPrefs)
    }

    private fun initScanPrefs() {
        contactsScanPrefs.setDependentView(useContactsPrefs)
        contactsScanPrefs.setOnClickListener { scanForBirthdays() }
        contactsScanPrefs.setDependentView(birthReminderPrefs)
    }

    private fun scanForBirthdays() {
        if (!Permissions.checkPermission(activity!!, Permissions.READ_CONTACTS)) {
            Permissions.requestPermission(activity!!, BIRTHDAYS_CODE, Permissions.READ_CONTACTS)
            return
        }
        val work = OneTimeWorkRequest.Builder(CheckBirthdaysWorker::class.java)
                .addTag("BD_CHECK_ONCE")
                .build()
        WorkManager.getInstance().enqueue(work)
    }

    private fun initContactsAutoPrefs() {
        autoScanPrefs.isChecked = prefs.isContactAutoCheckEnabled
        autoScanPrefs.setOnClickListener { changeAutoPrefs() }
        autoScanPrefs.setDependentView(useContactsPrefs)
        autoScanPrefs.setDependentView(birthReminderPrefs)
    }

    private fun changeAutoPrefs() {
        val isChecked = autoScanPrefs.isChecked
        autoScanPrefs.isChecked = !isChecked
        prefs.isContactAutoCheckEnabled = !isChecked
        if (!isChecked) {
            AlarmReceiver().enableBirthdayCheckAlarm()
        } else {
            AlarmReceiver().cancelBirthdayCheckAlarm()
        }
    }

    private fun initContactsPrefs() {
        useContactsPrefs.isChecked = prefs.isContactBirthdaysEnabled
        useContactsPrefs.setOnClickListener { changeContactsPrefs() }
        useContactsPrefs.setDependentView(birthReminderPrefs)
    }

    private fun changeContactsPrefs() {
        if (!Permissions.checkPermission(activity!!, Permissions.READ_CONTACTS)) {
            Permissions.requestPermission(activity!!, CONTACTS_CODE, Permissions.READ_CONTACTS)
            return
        }
        val isChecked = useContactsPrefs.isChecked
        useContactsPrefs.isChecked = !isChecked
        prefs.isContactBirthdaysEnabled = !isChecked
    }

    private fun initBirthdayTimePrefs() {
        reminderTimePrefs.setOnClickListener { showTimeDialog() }
        reminderTimePrefs.setValueText(prefs.birthdayTime)
        reminderTimePrefs.setDependentView(birthReminderPrefs)
    }

    private fun showTimeDialog() {
        val calendar = TimeUtil.getBirthdayCalendar(prefs.birthdayTime)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimeUtil.showTimePicker(context!!, themeUtil.dialogStyle, prefs.is24HourFormatEnabled, hour, minute, this)
    }

    private fun initDaysToPrefs() {
        daysToPrefs.setOnClickListener { showDaysToDialog() }
        daysToPrefs.setValue(prefs.daysToBirthday)
        daysToPrefs.setDependentView(birthReminderPrefs)
    }

    private fun showDaysToDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(R.string.days_to_birthday)
        val b = layoutInflater.inflate(R.layout.dialog_with_seek_and_title, null)
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
        builder.setView(b)
        builder.setPositiveButton(R.string.ok) { _, _ -> saveDays(b.seekBar.progress) }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity!!)
    }

    private fun saveDays(progress: Int) {
        prefs.daysToBirthday = progress
        initDaysToPrefs()
    }

    private fun initPermanentPrefs() {
        birthdayPermanentPrefs.isChecked = prefs.isBirthdayPermanentEnabled
        birthdayPermanentPrefs.setOnClickListener { changeBirthdayPermanentPrefs() }
        birthdayPermanentPrefs.setDependentView(birthReminderPrefs)
    }

    private fun changeBirthdayPermanentPrefs() {
        if (context == null) return
        val isChecked = birthdayPermanentPrefs.isChecked
        birthdayPermanentPrefs.isChecked = !isChecked
        prefs.isBirthdayPermanentEnabled = !isChecked
        if (!isChecked) {
            context?.sendBroadcast(Intent(context, PermanentBirthdayReceiver::class.java).setAction(PermanentBirthdayReceiver.ACTION_SHOW))
            AlarmReceiver().enableBirthdayPermanentAlarm(context!!)
        } else {
            context?.sendBroadcast(Intent(context, PermanentBirthdayReceiver::class.java).setAction(PermanentBirthdayReceiver.ACTION_HIDE))
            AlarmReceiver().cancelBirthdayPermanentAlarm(context!!)
        }
    }

    private fun initBirthdaysWidgetPrefs() {
        widgetShowPrefs.isChecked = prefs.isBirthdayInWidgetEnabled
        widgetShowPrefs.setOnClickListener { changeWidgetPrefs() }
        widgetShowPrefs.setDependentView(birthReminderPrefs)
    }

    private fun changeWidgetPrefs() {
        val isChecked = widgetShowPrefs.isChecked
        widgetShowPrefs.isChecked = !isChecked
        prefs.isBirthdayInWidgetEnabled = !isChecked
        updatesHelper.updateCalendarWidget()
        updatesHelper.updateWidget()
    }

    private fun initBirthdayReminderPrefs() {
        birthReminderPrefs.setOnClickListener { changeBirthdayPrefs() }
        birthReminderPrefs.isChecked = prefs.isBirthdayReminderEnabled
    }

    private fun changeBirthdayPrefs() {
        val isChecked = birthReminderPrefs.isChecked
        birthReminderPrefs.isChecked = !isChecked
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
        if (grantResults.isEmpty()) return
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

        private const val CONTACTS_CODE = 302
        private const val BIRTHDAYS_CODE = 303
    }
}
