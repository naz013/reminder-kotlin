package com.elementary.tasks.core.utils.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.View
import com.elementary.tasks.core.protocol.StartDayOfWeekProtocol
import com.elementary.tasks.core.utils.params.Prefs
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class DateTimePickerProvider(private val prefs: Prefs) {

  fun showTimePicker(
    context: Context,
    hour: Int,
    minute: Int,
    listener: TimePickerDialog.OnTimeSetListener
  ): TimePickerDialog {
    val dialog = TimePickerDialog(context, listener, hour, minute, prefs.is24HourFormat)
    dialog.show()
    return dialog
  }

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

  fun showDatePicker(
    context: Context,
    date: LocalDate,
    listener: (LocalDate) -> Unit
  ): DatePickerDialog {
    return showDatePicker(context, date, true, listener)
  }

  @SuppressLint("DiscouragedApi")
  fun showDatePicker(
    context: Context,
    date: LocalDate,
    showYear: Boolean,
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
    if (!showYear) {
      runCatching {
        dialog.datePicker.findViewById<View>(
          context.resources.getIdentifier("date_picker_header_year", "id", "android")
        ).visibility = View.GONE
      }
      runCatching {
        dialog.datePicker.findViewById<View>(
          context.resources.getIdentifier("year", "id", "android")
        ).visibility = View.GONE
      }
    }
    dialog.show()
    return dialog
  }
}
