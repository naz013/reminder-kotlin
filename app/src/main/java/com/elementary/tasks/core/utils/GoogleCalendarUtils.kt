package com.elementary.tasks.core.utils

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.text.TextUtils
import androidx.annotation.RequiresPermission
import com.elementary.tasks.R
import com.elementary.tasks.core.data.dao.CalendarEventsDao
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.readInt
import com.elementary.tasks.core.utils.io.readLong
import com.elementary.tasks.core.utils.io.readString
import com.elementary.tasks.core.utils.params.Prefs
import timber.log.Timber
import java.util.TimeZone

class GoogleCalendarUtils(
  private val context: Context,
  private val prefs: Prefs,
  private val calendarEventsDao: CalendarEventsDao,
  private val dateTimeManager: DateTimeManager
) {

  /**
   * Add event to calendar.
   */
  fun addEvent(reminder: Reminder) {
    val mId = reminder.calendarId
    if (mId != 0L) {
      val tz = TimeZone.getDefault()
      val timeZone = tz.displayName
      val cr = context.contentResolver
      val values = ContentValues()
      val startTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)?.let {
        dateTimeManager.toMillis(it)
      } ?: return
      values.put(CalendarContract.Events.DTSTART, startTime)
      values.put(CalendarContract.Events.DTEND, startTime + 60 * 1000 * prefs.calendarEventDuration)
      if (!TextUtils.isEmpty(reminder.summary)) {
        values.put(CalendarContract.Events.TITLE, reminder.summary)
      }
      values.put(CalendarContract.Events.CALENDAR_ID, mId)
      values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
      values.put(CalendarContract.Events.ALL_DAY, 0)
      values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
      values.put(CalendarContract.Events.DESCRIPTION, context.getString(R.string.from_reminder))
      val lEventUri = Uri.parse("content://com.android.calendar/events")
      val event: Uri?
      try {
        event = cr.insert(lEventUri, values)
        if (event != null) {
          val eventID = java.lang.Long.parseLong(event.lastPathSegment ?: "")
          calendarEventsDao.insert(CalendarEvent(reminder.uuId, event.toString(), eventID))
        }
      } catch (e: Exception) {
        Timber.d("addEvent: ${e.message}")
      }
    }
  }

  fun deleteEvents(id: String) {
    if (!Permissions.checkPermission(context, Permissions.WRITE_CALENDAR)) {
      return
    }
    val events = calendarEventsDao.getByReminder(id).toMutableList()
    val cr = context.contentResolver
    for (i in events.indices.reversed()) {
      val event = events.removeAt(i)
      cr.delete(
        /* url = */ CalendarContract.Events.CONTENT_URI,
        /* where = */ CalendarContract.Events._ID + "='" + event.eventId + "'",
        /* selectionArgs = */ null
      )
      calendarEventsDao.delete(event)
    }
  }

  fun deleteEvent(id: Long) {
    if (!Permissions.checkPermission(context, Permissions.WRITE_CALENDAR)) {
      return
    }
    val cr = context.contentResolver
    cr.delete(
      /* url = */ CalendarContract.Events.CONTENT_URI,
      /* where = */ CalendarContract.Events._ID + "='" + id + "'",
      /* selectionArgs = */ null
    )
  }

  fun loadEvents(reminderId: String): List<EventItem> {
    if (!Permissions.checkPermission(context, Permissions.WRITE_CALENDAR)) {
      return listOf()
    }
    val list = mutableListOf<EventItem>()
    val events = calendarEventsDao.getByReminder(reminderId).toMutableList()
    for (e in events) {
      val event = getEvent(e.eventId, e.uuId)
      if (event != null) {
        list.add(event)
      }
    }
    return list
  }

  @RequiresPermission(Permissions.READ_CALENDAR)
  fun getEvent(id: Long, uuId: String): EventItem? {
    if (id == 0L) {
      return null
    }
    try {
      val contentResolver = context.contentResolver
      val c = contentResolver.query(
        /* uri = */ CalendarContract.Events.CONTENT_URI,
        /* projection = */ arrayOf(
          CalendarContract.Events.TITLE,
          CalendarContract.Events.DESCRIPTION,
          CalendarContract.Events.DTSTART,
          CalendarContract.Events.DTEND,
          CalendarContract.Events.RRULE,
          CalendarContract.Events.RDATE,
          CalendarContract.Events._ID,
          CalendarContract.Events.CALENDAR_ID,
          CalendarContract.Events.ALL_DAY
        ),
        /* selection = */ CalendarContract.Events._ID + "='" + id + "'",
        /* selectionArgs = */ null,
        /* sortOrder = */ "dtstart ASC"
      )
      if (c != null && c.moveToFirst()) {
        val title = c.getString(c.getColumnIndex(CalendarContract.Events.TITLE)) ?: ""
        val description = c.getString(c.getColumnIndex(CalendarContract.Events.DESCRIPTION))
          ?: ""
        val rrule = c.getString(c.getColumnIndex(CalendarContract.Events.RRULE)) ?: ""
        val rDate = c.getString(c.getColumnIndex(CalendarContract.Events.RDATE)) ?: ""
        val calendarId = c.getLong(c.getColumnIndex(CalendarContract.Events.CALENDAR_ID))
        val allDay = c.getInt(c.getColumnIndex(CalendarContract.Events.ALL_DAY))
        val dtStart = c.getLong(c.getColumnIndex(CalendarContract.Events.DTSTART))
        val dtEnd = c.getLong(c.getColumnIndex(CalendarContract.Events.DTEND))
        val eventID = c.getLong(c.getColumnIndex(CalendarContract.Events._ID))

        c.close()

        return EventItem(
          title = title,
          description = description,
          rrule = rrule,
          rDate = rDate,
          calendarId = calendarId,
          allDay = allDay,
          dtStart = dtStart,
          dtEnd = dtEnd,
          id = eventID,
          localId = uuId
        )
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return null
  }

  /**
   * Add event to stock Android calendar.
   *
   * @param summary   summary.
   * @param startTime event start time in milliseconds.
   */
  fun addEventToStock(summary: String, startTime: Long) {
    val intent = Intent(Intent.ACTION_INSERT)
      .setData(CalendarContract.Events.CONTENT_URI)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
      .putExtra(
        CalendarContract.EXTRA_EVENT_END_TIME,
        startTime + 60 * 1000 * prefs.calendarEventDuration
      )
      .putExtra(CalendarContract.Events.TITLE, summary)
      .putExtra(CalendarContract.Events.DESCRIPTION, context.getString(R.string.from_reminder))
    try {
      context.startActivity(intent)
    } catch (ignored: ActivityNotFoundException) {
    }
  }

  fun getCalendarsList(): List<CalendarItem> {
    if (!Permissions.checkPermission(context, Permissions.READ_CALENDAR)) {
      return listOf()
    }
    val ids = mutableListOf<CalendarItem>()
    val uri = CalendarContract.Calendars.CONTENT_URI
    val projection = arrayOf(
      CalendarContract.Calendars._ID, // 0
      CalendarContract.Calendars.ACCOUNT_NAME, // 1
      CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, // 2
      CalendarContract.Calendars.OWNER_ACCOUNT // 3
    )
    var c: Cursor? = null
    try {
      c = context.contentResolver.query(uri, projection, null, null, null)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    if (c != null && c.moveToFirst()) {
      do {
        val mID = c.readLong(projection[0], 0L)
        val title = c.readString(projection[2]) ?: ""
        ids.add(CalendarItem(title, mID))
      } while (c.moveToNext())
    }
    c?.close()
    return ids.sortedBy { it.id }
  }

  fun getEvents(ids: List<Long>): List<EventItem> {
    if (ids.isEmpty()) return listOf()
    if (!Permissions.checkPermission(
        context,
        Permissions.READ_CALENDAR,
        Permissions.WRITE_CALENDAR
      )
    ) {
      return listOf()
    }
    val list = mutableListOf<EventItem>()
    try {
      val contentResolver = context.contentResolver
      for (id in ids) {
        val c = contentResolver.query(
          /* uri = */ CalendarContract.Events.CONTENT_URI,
          /* projection = */ arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.RRULE,
            CalendarContract.Events.RDATE,
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.ALL_DAY
          ),
          /* selection = */ CalendarContract.Events.CALENDAR_ID + "='" + id + "'",
          /* selectionArgs = */ null,
          /* sortOrder = */ "dtstart ASC"
        )
        if (c != null && c.moveToFirst()) {
          do {
            val title = c.readString(CalendarContract.Events.TITLE) ?: ""
            val description = c.readString(CalendarContract.Events.DESCRIPTION) ?: ""
            val rrule = c.readString(CalendarContract.Events.RRULE) ?: ""
            val rDate = c.readString(CalendarContract.Events.RDATE) ?: ""
            val calendarId = c.readLong(CalendarContract.Events.CALENDAR_ID, 0L)
            val allDay = c.readInt(CalendarContract.Events.ALL_DAY) ?: 0
            val dtStart = c.readLong(CalendarContract.Events.DTSTART, 0L)
            val dtEnd = c.readLong(CalendarContract.Events.DTEND, 0L)
            val eventID = c.readLong(CalendarContract.Events._ID, 0L)
            list.add(
              EventItem(
                title = title,
                description = description,
                rrule = rrule,
                rDate = rDate,
                calendarId = calendarId,
                allDay = allDay,
                dtStart = dtStart,
                dtEnd = dtEnd,
                id = eventID,
                localId = ""
              )
            )
          } while (c.moveToNext())
          c.close()
        }
      }
    } catch (_: Exception) {
    }
    return list
  }

  data class EventItem(
    val title: String,
    val description: String,
    val rrule: String,
    private val rDate: String,
    val calendarId: Long,
    val allDay: Int,
    val dtStart: Long,
    val dtEnd: Long,
    val id: Long,
    var localId: String = "",
    var calendarName: String = ""
  )

  data class CalendarItem(
    val name: String,
    val id: Long
  )
}
