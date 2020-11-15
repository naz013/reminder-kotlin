package com.elementary.tasks.navigation.settings.calendar

import android.app.AlarmManager
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.EventJobScheduler
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.FragmentSettingsEventsImportBinding
import com.elementary.tasks.navigation.settings.BaseCalendarFragment
import kotlinx.coroutines.Job
import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
import org.koin.android.ext.android.inject
import java.util.*

class FragmentEventsImport : BaseCalendarFragment<FragmentSettingsEventsImportBinding>(),
  CompoundButton.OnCheckedChangeListener {

  private val eventControlFactory by inject<EventControlFactory>()
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

  override fun layoutRes(): Int = R.layout.fragment_settings_events_import

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.button.setOnClickListener { importEvents() }

    binding.progressMessageView.text = getString(R.string.please_wait)
    binding.progressView.visibility = View.GONE

    binding.syncInterval.setOnClickListener { showIntervalDialog() }

    binding.autoCheck.setOnCheckedChangeListener(this)
    binding.autoCheck.isChecked = prefs.isAutoEventsCheckEnabled
    binding.syncInterval.isEnabled = false

    binding.eventCalendars.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    binding.eventCalendars.adapter = calendarsAdapter
  }

  private fun showIntervalDialog() {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      builder.setTitle(getString(R.string.interval))
      val items = arrayOf(getString(R.string.one_hour),
        getString(R.string.six_hours),
        getString(R.string.twelve_hours),
        getString(R.string.one_day),
        getString(R.string.two_days))
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
    withActivity {
      if (Permissions.checkPermission(it, AUTO_PERM, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
        startCheckService()
      }
    }
  }

  private fun startCheckService() {
    EventJobScheduler.scheduleEventCheck(prefs)
  }

  private fun loadCalendars() {
    withActivity {
      if (!Permissions.checkPermission(it, CALENDAR_PERM, Permissions.READ_CALENDAR)) {
        return@withActivity
      }
      list = calendarUtils.getCalendarsList()
      if (list.isEmpty()) {
        Toast.makeText(context, getString(R.string.no_calendars_found), Toast.LENGTH_SHORT).show()
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
    withActivity {
      if (!Permissions.checkPermission(it, 102, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
        return@withActivity
      }
      if (list.isEmpty()) {
        Toast.makeText(it, getString(R.string.no_calendars_found), Toast.LENGTH_SHORT).show()
        return@withActivity
      }
      val selectedIds = calendarsAdapter.getSelectedIds()
      if (selectedIds.isEmpty()) {
        Toast.makeText(it, getString(R.string.you_dont_select_any_calendar), Toast.LENGTH_SHORT).show()
        return@withActivity
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
        withActivity {
          if (Permissions.checkPermission(it, 101, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
            autoCheck(true)
          }
        }
      } else {
        autoCheck(false)
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (Permissions.checkPermission(grantResults)) {
      when (requestCode) {
        101 -> autoCheck(true)
        102 -> importEvents()
        CALENDAR_PERM -> loadCalendars()
        AUTO_PERM -> startCheckService()
      }
    }
  }

  private fun autoCheck(isChecked: Boolean) {
    prefs.isAutoEventsCheckEnabled = isChecked
    binding.syncInterval.isEnabled = isChecked
    if (isChecked) {
      EventJobScheduler.scheduleEventCheck(prefs)
    } else {
      EventJobScheduler.cancelEventCheck()
    }
  }

  private fun import(ids: Array<Long>) {
    val ctx = context ?: return
    binding.button.isEnabled = false
    binding.progressView.visibility = View.VISIBLE
    mJob = launchDefault {
      val currTime = System.currentTimeMillis()
      var eventsCount = 0
      val appDb = AppDb.getAppDatabase(ctx)
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
          Toast.makeText(ctx, getString(R.string.no_events_found), Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(ctx, "$eventsCount " + getString(R.string.events_found), Toast.LENGTH_SHORT).show()
          UpdatesHelper.updateCalendarWidget(ctx)
          Notifier.updateReminderPermanent(requireContext(), PermanentReminderReceiver.ACTION_SHOW)
        }
      }
    }
  }

  private fun saveReminder(itemId: Long, summary: String, dtStart: Long, repeat: Long,
                           categoryId: String, calendarId: Long, appDb: AppDb) {
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

  companion object {
    private const val CALENDAR_PERM = 500
    private const val AUTO_PERM = 501
  }
}
