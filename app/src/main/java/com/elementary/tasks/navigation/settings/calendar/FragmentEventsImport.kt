package com.elementary.tasks.navigation.settings.calendar

import android.app.AlarmManager
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.AlarmReceiver
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.navigation.settings.BaseCalendarFragment
import kotlinx.android.synthetic.main.fragment_settings_events_import.*
import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
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
class FragmentEventsImport : BaseCalendarFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private var mItemSelect: Int = 0
    private var list: List<CalendarUtils.CalendarItem> = listOf()

    private val intervalPosition: Int
        get() {
            val position: Int
            val interval = prefs.autoCheckInterval
            position = when (interval) {
                1 -> 0
                6 -> 1
                12 -> 2
                24 -> 3
                48 -> 4
                else -> 0
            }
            mItemSelect = position
            return position
        }

    override fun layoutRes(): Int = R.layout.fragment_settings_events_import

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener(this)

        syncInterval.setOnClickListener { showIntervalDialog() }

        autoCheck.setOnCheckedChangeListener(this)
        autoCheck.isChecked = prefs.isAutoEventsCheckEnabled
        syncInterval.isEnabled = false
    }

    private fun showIntervalDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.interval))
        val items = arrayOf<CharSequence>(getString(R.string.one_hour), getString(R.string.six_hours), getString(R.string.twelve_hours), getString(R.string.one_day), getString(R.string.two_days))
        builder.setSingleChoiceItems(items, intervalPosition) { _, item -> mItemSelect = item }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            saveIntervalPrefs()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun saveIntervalPrefs() {
        when (mItemSelect) {
            0 -> prefs.autoCheckInterval = 1
            1 -> prefs.autoCheckInterval = 6
            2 -> prefs.autoCheckInterval = 12
            3 -> prefs.autoCheckInterval = 24
            4 -> prefs.autoCheckInterval = 48
        }
        if (Permissions.checkPermission(context!!, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
            startCheckService()
        } else {
            Permissions.requestPermission(activity!!, AUTO_PERM, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)
        }
    }

    private fun startCheckService() {
        AlarmReceiver().enableEventCheck(context!!)
    }

    private fun checkWriteCalendarPerm(): Boolean {
        if (Permissions.checkPermission(activity!!, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
            return true
        } else {
            Permissions.requestPermission(activity!!, 102, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR);
            return false
        }
    }

    private fun loadCalendars() {
        if (!checkCalendarPerm()) {
            return
        }
        list = calendarUtils.getCalendarsList()
        if (list.isEmpty()) {
            Toast.makeText(context, getString(R.string.no_calendars_found), Toast.LENGTH_SHORT).show()
        }
        val spinnerArray = ArrayList<String>()
        spinnerArray.add(getString(R.string.choose_calendar))
        if (list.isNotEmpty()) {
            for (item in list) {
                spinnerArray.add(item.name)
            }
        }
        val spinnerArrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item, spinnerArray)
        eventCalendar.adapter = spinnerArrayAdapter
    }

    override fun onBackStackResume() {
        super.onBackStackResume()
        loadCalendars()
    }

    override fun getTitle(): String = getString(R.string.import_events)

    private fun checkCalendarPerm(): Boolean {
        return if (Permissions.checkPermission(activity!!, Permissions.READ_CALENDAR)) {
            true
        } else {
            Permissions.requestPermission(activity!!, CALENDAR_PERM, Permissions.READ_CALENDAR)
            false
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button -> importEvents()
        }
    }

    private fun importEvents() {
        if (!checkWriteCalendarPerm()) return
        if (list.isEmpty()) {
            Toast.makeText(context, getString(R.string.no_calendars_found), Toast.LENGTH_SHORT).show()
            return
        }
        if (eventCalendar.selectedItemPosition == 0) {
            Toast.makeText(context, getString(R.string.you_dont_select_any_calendar), Toast.LENGTH_SHORT).show()
            return
        }
        val map = HashMap<String, Int>()
        val selectedPosition = eventCalendar.selectedItemPosition - 1
        map[EVENT_KEY] = list[selectedPosition].id
        val isEnabled = prefs.isCalendarEnabled
        if (!isEnabled) {
            prefs.isCalendarEnabled = true
            prefs.calendarId = list[selectedPosition].id
        }
        prefs.eventsCalendar = list[selectedPosition].id
        Import(context!!).execute(map)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.autoCheck -> if (isChecked) {
                if (Permissions.checkPermission(activity!!, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
                    autoCheck(true)
                } else {
                    Permissions.requestPermission(activity!!, 101, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)
                }
            } else
                autoCheck(false)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) return
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
        prefs.isAutoEventsCheckEnabled = isChecked
        syncInterval.isEnabled = isChecked
        val alarm = AlarmReceiver()
        if (isChecked)
            alarm.enableEventCheck(context!!)
        else
            alarm.cancelEventCheck(context!!)
    }

    private inner class Import constructor(private val mContext: Context) : AsyncTask<HashMap<String, Int>, Void, Int>() {
        private var dialog: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
            dialog = ProgressDialog.show(mContext, null, getString(R.string.please_wait), true, false)
        }

        @SafeVarargs
        override fun doInBackground(vararg params: HashMap<String, Int>): Int {
            val currTime = System.currentTimeMillis()
            var eventsCount = 0
            val map = params[0]
            if (map.containsKey(EVENT_KEY)) {
                val eventItems = calendarUtils.getEvents(map[EVENT_KEY]!!)
                if (!eventItems.isEmpty()) {
                    val list = AppDb.getAppDatabase(mContext).calendarEventsDao().eventIds()
                    for (item in eventItems) {
                        val itemId = item.id
                        if (!list.contains(itemId)) {
                            val rrule = item.rrule
                            var repeat: Long = 0
                            if (rrule != "" && !rrule.matches("".toRegex())) {
                                try {
                                    val rule = RecurrenceRule(rrule)
                                    val interval = rule.interval
                                    val freq = rule.freq
                                    repeat = when {
                                        freq === Freq.SECONDLY -> interval * TimeCount.SECOND
                                        freq === Freq.MINUTELY -> interval * TimeCount.MINUTE
                                        freq === Freq.HOURLY -> interval * TimeCount.HOUR
                                        freq === Freq.WEEKLY -> interval.toLong() * 7 * TimeCount.DAY
                                        freq === Freq.MONTHLY -> interval.toLong() * 30 * TimeCount.DAY
                                        freq === Freq.YEARLY -> interval.toLong() * 365 * TimeCount.DAY
                                        else -> interval * TimeCount.DAY
                                    }
                                } catch (e: InvalidRecurrenceRuleException) {
                                    e.printStackTrace()
                                }
                            }
                            val summary = item.title
                            val group = AppDb.getAppDatabase(mContext).reminderGroupDao().defaultGroup()
                            var categoryId = ""
                            if (group != null) {
                                categoryId = group.groupUuId
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

        private fun saveReminder(itemId: Long, summary: String, dtStart: Long, repeat: Long, categoryId: String) {
            val reminder = Reminder()
            reminder.type = Reminder.BY_DATE
            reminder.repeatInterval = repeat
            reminder.groupUuId = categoryId
            reminder.summary = summary
            reminder.eventTime = TimeUtil.getGmtFromDateTime(dtStart)
            reminder.startTime = TimeUtil.getGmtFromDateTime(dtStart)
            AppDb.getAppDatabase(mContext).reminderDao().insert(reminder)
            EventControlFactory.getController(reminder).start()
            val event = CalendarEvent(reminder.uuId, summary, itemId)
            AppDb.getAppDatabase(mContext).calendarEventsDao().insert(event)
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
            if (result == 0) Toast.makeText(mContext, getString(R.string.no_events_found), Toast.LENGTH_SHORT).show()
            if (result > 0) {
                Toast.makeText(mContext, result.toString() + " " + getString(R.string.events_found), Toast.LENGTH_SHORT).show()
                updatesHelper.updateCalendarWidget()
                notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
            }
        }
    }

    companion object {

        const val EVENT_KEY = "Events"
        private const val CALENDAR_PERM = 500
        private const val AUTO_PERM = 501
    }
}
