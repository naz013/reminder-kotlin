package com.elementary.tasks.navigation.settings.calendar

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.AlarmReceiver
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.roboto.RoboButton
import com.elementary.tasks.core.views.roboto.RoboCheckBox
import com.elementary.tasks.databinding.FragmentEventsImportBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule

import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap

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
class FragmentEventsImport : BaseSettingsFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private var binding: FragmentEventsImportBinding? = null
    private var syncInterval: RoboButton? = null

    private var mItemSelect: Int = 0
    private var list: List<CalendarUtils.CalendarItem>? = null

    private val intervalPosition: Int
        get() {
            val position: Int
            val interval = prefs!!.autoCheckInterval
            when (interval) {
                1 -> position = 0
                6 -> position = 1
                12 -> position = 2
                24 -> position = 3
                48 -> position = 4
                else -> position = 0
            }
            mItemSelect = position
            return position
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEventsImportBinding.inflate(inflater, container, false)
        binding!!.button.setOnClickListener(this)

        syncInterval = binding!!.syncInterval
        syncInterval!!.setOnClickListener { v -> showIntervalDialog() }

        val autoCheck = binding!!.autoCheck
        autoCheck.setOnCheckedChangeListener(this)
        autoCheck.isChecked = prefs!!.isAutoEventsCheckEnabled
        syncInterval!!.isEnabled = false
        return binding!!.root
    }

    private fun showIntervalDialog() {
        val builder = Dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.interval))
        val items = arrayOf<CharSequence>(getString(R.string.one_hour), getString(R.string.six_hours), getString(R.string.twelve_hours), getString(R.string.one_day), getString(R.string.two_days))
        builder.setSingleChoiceItems(items, intervalPosition) { dialog, item -> mItemSelect = item }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, which ->
            saveIntervalPrefs()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { dialogInterface -> mItemSelect = 0 }
        dialog.setOnDismissListener { dialogInterface -> mItemSelect = 0 }
        dialog.show()
    }

    private fun saveIntervalPrefs() {
        if (mItemSelect == 0) {
            prefs!!.autoCheckInterval = 1
        } else if (mItemSelect == 1) {
            prefs!!.autoCheckInterval = 6
        } else if (mItemSelect == 2) {
            prefs!!.autoCheckInterval = 12
        } else if (mItemSelect == 3) {
            prefs!!.autoCheckInterval = 24
        } else if (mItemSelect == 4) {
            prefs!!.autoCheckInterval = 48
        }
        if (Permissions.checkPermission(context, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
            startCheckService()
        } else {
            Permissions.requestPermission(activity, AUTO_PERM, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)
        }
    }

    private fun startCheckService() {
        AlarmReceiver().enableEventCheck(context)
    }

    private fun loadCalendars() {
        list = CalendarUtils.getCalendarsList(context)
        if (list == null || list!!.size == 0) {
            Toast.makeText(context, getString(R.string.no_calendars_found), Toast.LENGTH_SHORT).show()
        }
        val spinnerArray = ArrayList<String>()
        spinnerArray.add(getString(R.string.choose_calendar))
        if (list != null && list!!.size > 0) {
            for (item in list!!) {
                spinnerArray.add(item.name)
            }
        }
        val spinnerArrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, spinnerArray)
        binding!!.eventCalendar.adapter = spinnerArrayAdapter
    }

    override fun onResume() {
        super.onResume()
        if (checkCalendarPerm()) {
            loadCalendars()
        }
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.import_events))
            callback!!.onFragmentSelect(this)
        }
    }

    private fun checkCalendarPerm(): Boolean {
        if (Permissions.checkPermission(activity, Permissions.READ_CALENDAR)) {
            return true
        } else {
            Permissions.requestPermission(activity, CALENDAR_PERM, Permissions.READ_CALENDAR)
            return false
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button -> if (Permissions.checkPermission(context, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
                importEvents()
            } else {
                Permissions.requestPermission(activity, 102, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)
            }
        }
    }

    private fun importEvents() {
        if (list == null || list!!.isEmpty()) {
            Toast.makeText(context, getString(R.string.no_calendars_found), Toast.LENGTH_SHORT).show()
            return
        }
        if (binding!!.eventCalendar.selectedItemPosition == 0) {
            Toast.makeText(context, getString(R.string.you_dont_select_any_calendar), Toast.LENGTH_SHORT).show()
            return
        }
        val map = HashMap<String, Int>()
        val selectedPosition = binding!!.eventCalendar.selectedItemPosition - 1
        map[EVENT_KEY] = list!![selectedPosition].id
        val isEnabled = prefs!!.isCalendarEnabled
        if (!isEnabled) {
            prefs!!.isCalendarEnabled = true
            prefs!!.calendarId = list!![selectedPosition].id
        }
        prefs!!.eventsCalendar = list!![selectedPosition].id
        Import(context).execute(map)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.autoCheck -> if (isChecked) {
                if (Permissions.checkPermission(activity, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
                    autoCheck(true)
                } else {
                    Permissions.requestPermission(activity, 101, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)
                }
            } else
                autoCheck(false)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            101 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                autoCheck(true)
            }
            102 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                importEvents()
            }
            CALENDAR_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadCalendars()
            }
            AUTO_PERM -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCheckService()
            }
        }
    }

    private fun autoCheck(isChecked: Boolean) {
        prefs!!.isAutoEventsCheckEnabled = isChecked
        syncInterval!!.isEnabled = isChecked
        val alarm = AlarmReceiver()
        if (isChecked)
            alarm.enableEventCheck(context)
        else
            alarm.cancelEventCheck(context)
    }

    private inner class Import internal constructor(private val mContext: Context) : AsyncTask<HashMap<String, Int>, Void, Int>() {
        private var dialog: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
            dialog = ProgressDialog.show(mContext, null, getString(R.string.please_wait), true, false)
        }

        @SafeVarargs
        override fun doInBackground(vararg params: HashMap<String, Int>): Int? {
            if (params == null) {
                return 0
            }
            val currTime = System.currentTimeMillis()
            var eventsCount = 0
            val map = params[0]
            if (map.containsKey(EVENT_KEY)) {
                val eventItems = CalendarUtils.getEvents(mContext, map[EVENT_KEY])
                if (!eventItems.isEmpty()) {
                    val list = AppDb.getAppDatabase(mContext).calendarEventsDao().eventIds
                    for (item in eventItems) {
                        val itemId = item.id
                        if (!list.contains(itemId)) {
                            val rrule = item.rrule
                            var repeat: Long = 0
                            if (rrule != null && !rrule.matches("".toRegex())) {
                                try {
                                    val rule = RecurrenceRule(rrule)
                                    val interval = rule.interval
                                    val freq = rule.freq
                                    if (freq === Freq.SECONDLY)
                                        repeat = interval * TimeCount.SECOND
                                    else if (freq === Freq.MINUTELY)
                                        repeat = interval * TimeCount.MINUTE
                                    else if (freq === Freq.HOURLY)
                                        repeat = interval * TimeCount.HOUR
                                    else if (freq === Freq.WEEKLY)
                                        repeat = interval.toLong() * 7 * TimeCount.DAY
                                    else if (freq === Freq.MONTHLY)
                                        repeat = interval.toLong() * 30 * TimeCount.DAY
                                    else if (freq === Freq.YEARLY)
                                        repeat = interval.toLong() * 365 * TimeCount.DAY
                                    else
                                        repeat = interval * TimeCount.DAY
                                } catch (e: InvalidRecurrenceRuleException) {
                                    e.printStackTrace()
                                }

                            }
                            val summary = item.title
                            val group = AppDb.getAppDatabase(mContext).groupDao().default
                            var categoryId: String? = ""
                            if (group != null) {
                                categoryId = group.uuId
                            }
                            val calendar = Calendar.getInstance()
                            var dtStart = item.dtStart
                            calendar.timeInMillis = dtStart
                            if (dtStart >= currTime) {
                                eventsCount += 1
                                saveReminder(itemId, summary, dtStart, repeat, categoryId)
                            } else {
                                if (repeat > 0) {
                                    do {
                                        calendar.timeInMillis = dtStart + repeat * AlarmManager.INTERVAL_DAY
                                        dtStart = calendar.timeInMillis
                                    } while (dtStart < currTime)
                                    eventsCount += 1
                                    saveReminder(itemId, summary, dtStart, repeat, categoryId)
                                }
                            }
                        }
                    }
                }
            }
            return eventsCount
        }

        private fun saveReminder(itemId: Long, summary: String, dtStart: Long, repeat: Long, categoryId: String?) {
            val reminder = Reminder()
            reminder.type = Reminder.BY_DATE
            reminder.repeatInterval = repeat
            reminder.groupUuId = categoryId
            reminder.summary = summary
            reminder.eventTime = TimeUtil.getGmtFromDateTime(dtStart)
            reminder.startTime = TimeUtil.getGmtFromDateTime(dtStart)
            AppDb.getAppDatabase(mContext).reminderDao().insert(reminder)
            EventControlFactory.getController(reminder).start()
            val event = CalendarEvent(reminder.uniqueId, summary, itemId)
            AppDb.getAppDatabase(mContext).calendarEventsDao().insert(event)
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
            if (result == 0) Toast.makeText(mContext, getString(R.string.no_events_found), Toast.LENGTH_SHORT).show()
            if (result > 0) {
                Toast.makeText(mContext, result.toString() + " " + getString(R.string.events_found), Toast.LENGTH_SHORT).show()
                UpdatesHelper.getInstance(mContext).updateCalendarWidget()
                Notifier.updateReminderPermanent(mContext, PermanentReminderReceiver.ACTION_SHOW)
            }
        }
    }

    companion object {

        val EVENT_KEY = "Events"
        private val CALENDAR_PERM = 500
        private val AUTO_PERM = 501
    }
}
