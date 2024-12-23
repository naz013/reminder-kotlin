package com.elementary.tasks.core.utils.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.fragment.app.FragmentManager
import com.github.naz013.domain.calendar.StartDayOfWeekProtocol
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.calendarext.getDayOfMonth
import com.github.naz013.calendarext.getMonth
import com.github.naz013.calendarext.getYear
import com.github.naz013.calendarext.newCalendar
import com.github.naz013.calendarext.setDayOfMonth
import com.github.naz013.calendarext.setHour
import com.github.naz013.calendarext.setMinute
import com.github.naz013.calendarext.setMonth
import com.github.naz013.calendarext.setYear
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class DateTimePickerProvider(private val prefs: Prefs) {

  fun showTimePicker(
    fragmentManager: FragmentManager,
    time: LocalTime,
    title: String,
    listener: (LocalTime) -> Unit
  ): MaterialTimePicker {
    return MaterialTimePicker.Builder()
      .setTimeFormat(
        if (prefs.is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
      )
      .setHour(time.hour)
      .setMinute(time.minute)
      .setTitleText(title)
      .build()
      .also { picker ->
        picker.addOnPositiveButtonClickListener {
          listener(LocalTime.of(picker.hour, picker.minute))
        }
        picker.show(fragmentManager, "time_picker")
      }
  }

  @Deprecated("Use showTimePicker instead")
  fun showTimePicker(
    context: Context,
    time: LocalTime,
    listener: (LocalTime) -> Unit
  ): TimePickerDialog {
    val dialog = TimePickerDialog(context, { _, h, m ->
      listener.invoke(LocalTime.of(h, m))
    }, time.hour, time.minute, prefs.is24HourFormat)
    dialog.show()
    return dialog
  }

  @Deprecated("Use showDatePicker instead")
  fun showDatePicker(
    context: Context,
    date: LocalDate,
    listener: (LocalDate) -> Unit
  ): DatePickerDialog {
    val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
      listener.invoke(LocalDate.of(year, month + 1, dayOfMonth))
    }
    val dialog = DatePickerDialog(
      /* context = */ context,
      /* listener = */ dateListener,
      /* year = */ date.year,
      /* month = */ date.monthValue - 1,
      /* dayOfMonth = */ date.dayOfMonth
    )
    dialog.datePicker.firstDayOfWeek = StartDayOfWeekProtocol(prefs.startDay).getForDatePicker()
    dialog.show()
    return dialog
  }

  fun showDatePicker(
    fragmentManager: FragmentManager,
    date: LocalDate,
    title: String,
    listener: (LocalDate) -> Unit
  ): MaterialDatePicker<Long> {
    return MaterialDatePicker.Builder.datePicker()
      .setTitleText(title)
      .setSelection(
        newCalendar()
          .setYear(date.year)
          .setMonth(date.monthValue - 1)
          .setDayOfMonth(date.dayOfMonth)
          .setHour(12)
          .setMinute(0)
          .timeInMillis
      )
      .setCalendarConstraints(
        CalendarConstraints.Builder()
          .setFirstDayOfWeek(StartDayOfWeekProtocol(prefs.startDay).getForDatePicker())
          .build()
      )
      .build()
      .also { picker ->
        picker.addOnPositiveButtonClickListener {
          picker.selection?.let { newCalendar(it) }
            ?.also { calendar ->
              listener(
                LocalDate.of(
                  calendar.getYear(),
                  calendar.getMonth() + 1,
                  calendar.getDayOfMonth()
                )
              )
            }
        }
        picker.show(fragmentManager, "date_picker")
      }
  }
}
