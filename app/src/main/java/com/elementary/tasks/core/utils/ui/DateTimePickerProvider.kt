package com.elementary.tasks.core.utils.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.calendarext.getDayOfMonth
import com.github.naz013.calendarext.getHourOfDay
import com.github.naz013.calendarext.getMinute
import com.github.naz013.calendarext.getMonth
import com.github.naz013.calendarext.getYear
import com.github.naz013.calendarext.newCalendar
import com.github.naz013.calendarext.setDate
import com.github.naz013.calendarext.setTime
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.util.Calendar

class DateTimePickerProvider(
  private val prefs: Prefs
) {

  fun showTimePicker(
    context: Context, hour: Int, minute: Int,
    listener: TimePickerDialog.OnTimeSetListener
  ): TimePickerDialog {
    val dialog = TimePickerDialog(context, listener, hour, minute, prefs.is24HourFormat)
    dialog.show()
    return dialog
  }

  fun showTimePicker(
    context: Context, time: LocalTime,
    listener: (LocalTime) -> Unit
  ): TimePickerDialog {
    val dialog = TimePickerDialog(context, { _, h, m ->
      listener.invoke(LocalTime.of(h, m))
    }, time.hour, time.minute, prefs.is24HourFormat)
    dialog.show()
    return dialog
  }

  fun showTimePicker(
    context: Context, old: Calendar?,
    listener: (Calendar) -> Unit
  ): TimePickerDialog {
    val calendar = old ?: newCalendar()
    val dialog = TimePickerDialog(
      context,
      { _, hourOfDay, minute ->
        newCalendar()
          .setTime(hourOfDay, minute)
          .also { listener.invoke(it) }
      },
      calendar.getHourOfDay(),
      calendar.getMinute(),
      prefs.is24HourFormat
    )
    dialog.show()
    return dialog
  }

  fun showDatePicker(
    context: Context, year: Int, month: Int, dayOfMonth: Int,
    listener: DatePickerDialog.OnDateSetListener
  ): DatePickerDialog {
    val dialog = DatePickerDialog(context, listener, year, month, dayOfMonth)
    dialog.datePicker.firstDayOfWeek = prefs.startDay + 1
    dialog.show()
    return dialog
  }

  fun showDatePicker(
    context: Context, date: LocalDate,
    listener: (LocalDate) -> Unit
  ): DatePickerDialog {
    val dialog = DatePickerDialog(
      context,
      { _, year, month, dayOfMonth ->
        listener.invoke(LocalDate.of(year, month + 1, dayOfMonth))
      }, date.year, date.monthValue - 1, date.dayOfMonth
    )
    dialog.datePicker.firstDayOfWeek = prefs.startDay + 1
    dialog.show()
    return dialog
  }

  fun showDatePicker(
    context: Context, old: Calendar?,
    listener: (Calendar) -> Unit
  ): DatePickerDialog {
    val calendar = old ?: newCalendar()
    val dialog = DatePickerDialog(
      context,
      { _, year, monthOfYear, dayOfMonth ->
        newCalendar()
          .setDate(year, monthOfYear, dayOfMonth)
          .also { listener.invoke(it) }
      },
      calendar.getYear(),
      calendar.getMonth(),
      calendar.getDayOfMonth()
    )
    dialog.datePicker.firstDayOfWeek = prefs.startDay + 1
    dialog.show()
    return dialog
  }
}