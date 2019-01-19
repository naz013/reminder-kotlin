package com.elementary.tasks.navigation.settings.calendar

import android.app.AlarmManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.appWidgets.UpdatesHelper
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.AlarmReceiver
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.navigation.settings.BaseCalendarFragment
import kotlinx.android.synthetic.main.fragment_settings_events_import.*
import kotlinx.android.synthetic.main.view_progress.*
import kotlinx.coroutines.Job
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
class FragmentEventsImport : BaseCalendarFragment(), CompoundButton.OnCheckedChangeListener {

    private var mItemSelect: Int = 0
    private var list: List<CalendarUtils.CalendarItem> = listOf()
    private var mJob: Job? = null

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
        button.setOnClickListener{ importEvents() }

        progressMessageView.text = getString(R.string.please_wait)
        progressView.visibility = View.GONE

        syncInterval.setOnClickListener { showIntervalDialog() }

        autoCheck.setOnCheckedChangeListener(this)
        autoCheck.isChecked = prefs.isAutoEventsCheckEnabled
        syncInterval.isEnabled = false
    }

    private fun showIntervalDialog() {
        val builder = dialogues.getDialog(context!!)
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
        return if (Permissions.checkPermission(activity!!, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
            true
        } else {
            Permissions.requestPermission(activity!!, 102, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR);
            false
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
        val spinnerArrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, spinnerArray)
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
        import(map)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.autoCheck -> if (isChecked) {
                if (Permissions.checkPermission(activity!!, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
                    autoCheck(true)
                } else {
                    Permissions.requestPermission(activity!!, 101, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)
                }
            } else {
                autoCheck(false)
            }
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
        if (isChecked) {
            alarm.enableEventCheck(context!!)
        } else {
            alarm.cancelEventCheck(context!!)
        }
    }

    private fun import(map: HashMap<String, Int>) {
        val ctx = context ?: return
        button.isEnabled = false
        progressView.visibility = View.VISIBLE
        mJob = launchDefault {
            val currTime = System.currentTimeMillis()
            var eventsCount = 0
            val appDb = AppDb.getAppDatabase(ctx)
            if (map.containsKey(EVENT_KEY)) {
                val eventItems = calendarUtils.getEvents(map[EVENT_KEY]!!)
                if (!eventItems.isEmpty()) {
                    val list = appDb.calendarEventsDao().eventIds()
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
                            val group = appDb.reminderGroupDao().defaultGroup()
                            var categoryId = ""
                            if (group != null) {
                                categoryId = group.groupUuId
                            }
                            val calendar = Calendar.getInstance()
                            var dtStart = item.dtStart
                            calendar.timeInMillis = dtStart
                            if (dtStart >= currTime) {
                                eventsCount += 1
                                saveReminder(itemId, summary, dtStart, repeat, categoryId, appDb)
                            } else {
                                if (repeat > 0) {
                                    do {
                                        calendar.timeInMillis = dtStart + repeat * AlarmManager.INTERVAL_DAY
                                        dtStart = calendar.timeInMillis
                                    } while (dtStart < currTime)
                                    eventsCount += 1
                                    saveReminder(itemId, summary, dtStart, repeat, categoryId, appDb)
                                }
                            }
                        }
                    }
                }
            }

            withUIContext {
                button.isEnabled = true
                progressView.visibility = View.GONE

                if (eventsCount == 0) {
                    Toast.makeText(ctx, getString(R.string.no_events_found), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(ctx, "$eventsCount " + getString(R.string.events_found), Toast.LENGTH_SHORT).show()
                    UpdatesHelper.updateCalendarWidget(ctx)
                    notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
                }
            }
        }
    }

    private fun saveReminder(itemId: Long, summary: String, dtStart: Long, repeat: Long, categoryId: String, appDb: AppDb) {
        val reminder = Reminder()
        reminder.type = Reminder.BY_DATE
        reminder.repeatInterval = repeat
        reminder.groupUuId = categoryId
        reminder.summary = summary
        reminder.eventTime = TimeUtil.getGmtFromDateTime(dtStart)
        reminder.startTime = TimeUtil.getGmtFromDateTime(dtStart)
        appDb.reminderDao().insert(reminder)
        EventControlFactory.getController(reminder).start()
        appDb.calendarEventsDao().insert(CalendarEvent(reminder.uuId, summary, itemId))
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob?.cancel()
    }

    companion object {

        const val EVENT_KEY = "Events"
        private const val CALENDAR_PERM = 500
        private const val AUTO_PERM = 501
    }
}
