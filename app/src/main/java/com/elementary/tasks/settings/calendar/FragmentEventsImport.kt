package com.elementary.tasks.settings.calendar

import android.app.AlarmManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.FragmentSettingsEventsImportBinding
import com.elementary.tasks.settings.BaseCalendarFragment
import kotlinx.coroutines.Job
import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
import org.koin.android.ext.android.inject
import java.util.Calendar

class FragmentEventsImport : BaseCalendarFragment<FragmentSettingsEventsImportBinding>(),
  CompoundButton.OnCheckedChangeListener {

  private val eventControlFactory by inject<EventControlFactory>()
  private val appDb by inject<AppDb>()
  private val jobScheduler by inject<JobScheduler>()
  private val updatesHelper by inject<UpdatesHelper>()

  private val calendarsAdapter = CalendarsAdapter()
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

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsEventsImportBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.button.setOnClickListener { importEvents() }

    binding.progressMessageView.text = getString(R.string.please_wait)
    binding.progressView.visibility = View.GONE

    binding.syncInterval.setOnClickListener { showIntervalDialog() }

    binding.autoCheck.setOnCheckedChangeListener(this)
    binding.autoCheck.isChecked = prefs.isAutoEventsCheckEnabled
    binding.syncInterval.isEnabled = false

    binding.eventCalendars.layoutManager =
      LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    binding.eventCalendars.adapter = calendarsAdapter
  }

  private fun showIntervalDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(getString(R.string.interval))
      val items = arrayOf(
        getString(R.string.one_hour),
        getString(R.string.six_hours),
        getString(R.string.twelve_hours),
        getString(R.string.one_day),
        getString(R.string.two_days)
      )
      builder.setSingleChoiceItems(items, intervalPosition) { _, item -> mItemSelect = item }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        saveIntervalPrefs()
        dialog.dismiss()
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.create().show()
    }
  }

  private fun saveIntervalPrefs() {
    when (mItemSelect) {
      0 -> prefs.autoCheckInterval = 1
      1 -> prefs.autoCheckInterval = 6
      2 -> prefs.autoCheckInterval = 12
      3 -> prefs.autoCheckInterval = 24
      4 -> prefs.autoCheckInterval = 48
    }
    permissionFlow.askPermissions(listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      startCheckService()
    }
  }

  private fun startCheckService() {
    jobScheduler.scheduleEventCheck()
  }

  private fun loadCalendars() {
    permissionFlow.askPermission(Permissions.READ_CALENDAR) {
      list = calendarUtils.getCalendarsList()
      if (list.isEmpty()) {
        toast(R.string.no_calendars_found)
      }
      calendarsAdapter.data = list
      calendarsAdapter.selectIds(prefs.trackCalendarIds)
    }
  }

  override fun onBackStackResume() {
    super.onBackStackResume()
    loadCalendars()
  }

  override fun getTitle(): String = getString(R.string.import_events)

  private fun importEvents() {
    permissionFlow.askPermissions(listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
      if (list.isEmpty()) {
        toast(R.string.no_calendars_found)
        return@askPermissions
      }
      val selectedIds = calendarsAdapter.getSelectedIds()
      if (selectedIds.isEmpty()) {
        toast(R.string.you_dont_select_any_calendar)
        return@askPermissions
      }
      val isEnabled = prefs.isCalendarEnabled
      if (!isEnabled) {
        prefs.isCalendarEnabled = true
        prefs.defaultCalendarId = selectedIds[0]
      }
      prefs.trackCalendarIds = selectedIds
      import(selectedIds)
    }
  }

  override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
    when (buttonView.id) {
      R.id.autoCheck -> if (isChecked) {
        permissionFlow.askPermissions(
          listOf(Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)
        ) { autoCheck(true) }
      } else {
        autoCheck(false)
      }
    }
  }

  private fun autoCheck(isChecked: Boolean) {
    prefs.isAutoEventsCheckEnabled = isChecked
    binding.syncInterval.isEnabled = isChecked
    if (isChecked) {
      jobScheduler.scheduleEventCheck()
    } else {
      jobScheduler.cancelEventCheck()
    }
  }

  private fun import(ids: Array<Long>) {
    binding.button.isEnabled = false
    binding.progressView.visibility = View.VISIBLE
    mJob = launchDefault {
      val currTime = System.currentTimeMillis()
      var eventsCount = 0
      val eventItems = calendarUtils.getEvents(ids)
      if (eventItems.isNotEmpty()) {
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
              saveReminder(itemId, summary, dtStart, repeat, categoryId, item.calendarId, appDb)
            } else {
              if (repeat > 0) {
                do {
                  calendar.timeInMillis = dtStart + repeat * AlarmManager.INTERVAL_DAY
                  dtStart = calendar.timeInMillis
                } while (dtStart < currTime)
                eventsCount += 1
                saveReminder(itemId, summary, dtStart, repeat, categoryId, item.calendarId, appDb)
              }
            }
          }
        }
      }

      withUIContext {
        binding.button.isEnabled = true
        binding.progressView.visibility = View.GONE

        if (eventsCount == 0) {
          toast(getString(R.string.no_events_found))
        } else {
          toast("$eventsCount " + getString(R.string.events_found))
          updatesHelper.updateCalendarWidget()
          PermanentReminderReceiver.show(requireContext())
        }
      }
    }
  }

  private fun saveReminder(
    itemId: Long, summary: String, dtStart: Long, repeat: Long,
    categoryId: String, calendarId: Long, appDb: AppDb
  ) {
    val reminder = Reminder()
    reminder.type = Reminder.BY_DATE
    reminder.repeatInterval = repeat
    reminder.groupUuId = categoryId
    reminder.summary = summary
    reminder.calendarId = calendarId
    reminder.eventTime = TimeUtil.getGmtFromDateTime(dtStart)
    reminder.startTime = TimeUtil.getGmtFromDateTime(dtStart)
    appDb.reminderDao().insert(reminder)
    eventControlFactory.getController(reminder).start()
    appDb.calendarEventsDao().insert(CalendarEvent(reminder.uuId, summary, itemId))
  }

  override fun onDestroy() {
    super.onDestroy()
    mJob?.cancel()
  }
}
