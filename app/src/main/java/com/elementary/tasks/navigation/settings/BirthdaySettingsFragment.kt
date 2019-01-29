package com.elementary.tasks.navigation.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TimePicker
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import kotlinx.android.synthetic.main.dialog_with_seek_and_title.view.*
import kotlinx.android.synthetic.main.fragment_settings_birthdays_settings.*
import kotlinx.android.synthetic.main.view_progress.*
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
    private var mItemSelect: Int = 0

    private val onProgress: (Boolean) -> Unit = {
        if (it) {
            progressMessageView.text = getString(R.string.please_wait)
            scanButton.isEnabled = false
            progressView.visibility = View.VISIBLE
        } else {
            progressView.visibility = View.INVISIBLE
            scanButton.isEnabled = true
        }
    }

    override fun layoutRes(): Int = R.layout.fragment_settings_birthdays_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
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
        priorityPrefs.setOnClickListener { showPriorityDialog() }
        priorityPrefs.setDependentView(birthReminderPrefs)
        showPriority()
    }

    private fun showPriorityDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setTitle(getString(R.string.default_priority))
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, priorityList())
        mItemSelect = prefs.birthdayPriority
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which ->
            mItemSelect = which
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.birthdayPriority = mItemSelect
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnDismissListener { showPriority() }
        dialog.show()
    }

    private fun showPriority() {
        priorityPrefs.setDetailText(priorityList()[prefs.birthdayPriority])
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
        birthdayNotificationPrefs.setOnClickListener { callback?.openFragment(BirthdayNotificationFragment(), getString(R.string.birthday_notification)) }
        birthdayNotificationPrefs.setDependentView(birthReminderPrefs)
    }

    private fun initScanButton() {
        if (prefs.isContactBirthdaysEnabled) {
            scanButton.isEnabled = true
            scanButton.visibility = View.VISIBLE
            scanButton.setOnClickListener { scanForBirthdays() }
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
            scanButton.visibility = View.GONE
        }
    }

    private fun scanForBirthdays() {
        if (!Permissions.ensurePermissions(activity!!, BIRTHDAYS_CODE, Permissions.READ_CONTACTS)) {
            return
        }
        onProgress.invoke(true)
        ScanContactsWorker.scan(context!!)
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
        if (!Permissions.ensurePermissions(activity!!, CONTACTS_CODE, Permissions.READ_CONTACTS)) {
            return
        }
        val isChecked = useContactsPrefs.isChecked
        useContactsPrefs.isChecked = !isChecked
        prefs.isContactBirthdaysEnabled = !isChecked
        initScanButton()
    }

    private fun initBirthdayTimePrefs() {
        reminderTimePrefs.setOnClickListener { showTimeDialog() }
        reminderTimePrefs.setValueText(TimeUtil.getBirthdayVisualTime(prefs.birthdayTime, prefs.is24HourFormat, prefs.appLanguage))
        reminderTimePrefs.setDependentView(birthReminderPrefs)
    }

    private fun showTimeDialog() {
        val calendar = TimeUtil.getBirthdayCalendar(prefs.birthdayTime)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimeUtil.showTimePicker(context!!, themeUtil.dialogStyle, prefs.is24HourFormat, hour, minute, this)
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
        UpdatesHelper.updateCalendarWidget(context!!)
        UpdatesHelper.updateWidget(context!!)
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
