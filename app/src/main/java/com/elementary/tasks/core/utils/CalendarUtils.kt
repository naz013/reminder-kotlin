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
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.CalendarEvent
import com.elementary.tasks.core.data.models.Reminder
import timber.log.Timber
import java.util.*

class CalendarUtils(private val context: Context, private val prefs: Prefs, private val appDb: AppDb){

    /**
     * Add event to calendar.
     */
    fun addEvent(reminder: Reminder) {
        val mId = prefs.calendarId
        if (mId != 0) {
            val tz = TimeZone.getDefault()
            val timeZone = tz.displayName
            val cr = context.contentResolver
            val values = ContentValues()
            val startTime = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
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
                    appDb.calendarEventsDao().insert(CalendarEvent(reminder.uuId, event.toString(), eventID))
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
        val events = appDb.calendarEventsDao().getByReminder(id).toMutableList()
        val cr = context.contentResolver
        for (i in events.indices.reversed()) {
            val event = events.removeAt(i)
            cr.delete(CalendarContract.Events.CONTENT_URI,
                    CalendarContract.Events._ID + "='" + event.eventId + "'", null)
            appDb.calendarEventsDao().delete(event)
        }
    }

    fun deleteEvent(id: Long) {
        if (!Permissions.checkPermission(context, Permissions.WRITE_CALENDAR)) {
            return
        }
        val cr = context.contentResolver
        cr.delete(CalendarContract.Events.CONTENT_URI, CalendarContract.Events._ID + "='" + id + "'", null)
    }

    fun loadEvents(reminderId: String): List<EventItem> {
        if (!Permissions.checkPermission(context, Permissions.WRITE_CALENDAR)) {
            return listOf()
        }
        val list = mutableListOf<EventItem>()
        val events = appDb.calendarEventsDao().getByReminder(reminderId).toMutableList()
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
        if (id == 0L) return null
        try {
            val contentResolver = context.contentResolver
            val c = contentResolver.query(CalendarContract.Events.CONTENT_URI,
                    arrayOf(
                            CalendarContract.Events.TITLE,
                            CalendarContract.Events.DESCRIPTION,
                            CalendarContract.Events.DTSTART,
                            CalendarContract.Events.DTEND,
                            CalendarContract.Events.RRULE,
                            CalendarContract.Events.RDATE,
                            CalendarContract.Events._ID,
                            CalendarContract.Events.CALENDAR_ID,
                            CalendarContract.Events.ALL_DAY
                    ), CalendarContract.Events._ID+ "='" + id + "'", null, "dtstart ASC")
            if (c != null && c.moveToFirst()) {
                val title = c.getString(c.getColumnIndex(CalendarContract.Events.TITLE)) ?: ""
                val description = c.getString(c.getColumnIndex(CalendarContract.Events.DESCRIPTION)) ?: ""
                val rrule = c.getString(c.getColumnIndex(CalendarContract.Events.RRULE)) ?: ""
                val rDate = c.getString(c.getColumnIndex(CalendarContract.Events.RDATE)) ?: ""
                val calendarId = c.getInt(c.getColumnIndex(CalendarContract.Events.CALENDAR_ID))
                val allDay = c.getInt(c.getColumnIndex(CalendarContract.Events.ALL_DAY))
                val dtStart = c.getLong(c.getColumnIndex(CalendarContract.Events.DTSTART))
                val dtEnd = c.getLong(c.getColumnIndex(CalendarContract.Events.DTEND))
                val eventID = c.getLong(c.getColumnIndex(CalendarContract.Events._ID))

                c.close()

                return EventItem(title, description, rrule, rDate,
                        calendarId, allDay, dtStart, dtEnd, eventID, uuId)
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
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + 60 * 1000 * prefs.calendarEventDuration)
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
        val mProjection = arrayOf(CalendarContract.Calendars._ID, // 0
                CalendarContract.Calendars.ACCOUNT_NAME, // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        )
        var c: Cursor? = null
        try {
            c = context.contentResolver.query(uri, mProjection, null, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (c != null && c.moveToFirst()) {
            do {
                val mID = c.getInt(c.getColumnIndex(mProjection[0]))
                val title = c.getString(c.getColumnIndex(mProjection[2])) ?: ""
                ids.add(CalendarItem(title, mID))
            } while (c.moveToNext())
        }
        c?.close()
        return ids
    }

    fun getEvents(id: Int): List<EventItem> {
        val list = ArrayList<EventItem>()
        if (!Permissions.checkPermission(context, Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR)) {
            return list
        }
        try {
            val contentResolver = context.contentResolver
            val c = contentResolver.query(CalendarContract.Events.CONTENT_URI,
                    arrayOf(CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.RRULE, CalendarContract.Events.RDATE, CalendarContract.Events._ID, CalendarContract.Events.CALENDAR_ID, CalendarContract.Events.ALL_DAY),
                    CalendarContract.Events.CALENDAR_ID + "='" + id + "'", null, "dtstart ASC")
            if (c != null && c.moveToFirst()) {
                do {
                    val title = c.getString(c.getColumnIndex(CalendarContract.Events.TITLE)) ?: ""
                    val description = c.getString(c.getColumnIndex(CalendarContract.Events.DESCRIPTION)) ?: ""
                    val rrule = c.getString(c.getColumnIndex(CalendarContract.Events.RRULE)) ?: ""
                    val rDate = c.getString(c.getColumnIndex(CalendarContract.Events.RDATE)) ?: ""
                    val calendarId = c.getInt(c.getColumnIndex(CalendarContract.Events.CALENDAR_ID))
                    val allDay = c.getInt(c.getColumnIndex(CalendarContract.Events.ALL_DAY))
                    val dtStart = c.getLong(c.getColumnIndex(CalendarContract.Events.DTSTART))
                    val dtEnd = c.getLong(c.getColumnIndex(CalendarContract.Events.DTEND))
                    val eventID = c.getLong(c.getColumnIndex(CalendarContract.Events._ID))
                    list.add(EventItem(title, description, rrule, rDate,
                            calendarId, allDay, dtStart, dtEnd, eventID))
                } while (c.moveToNext())
                c.close()
            }
        } catch (e: Exception) {
        }
        return list
    }

    data class EventItem(val title: String, val description: String, val rrule: String,
                         private val rDate: String, val calendarID: Int, val allDay: Int,
                         val dtStart: Long, val dtEnd: Long, val id: Long, var localId: String = "")

    data class CalendarItem(val name: String, val id: Int)
}
