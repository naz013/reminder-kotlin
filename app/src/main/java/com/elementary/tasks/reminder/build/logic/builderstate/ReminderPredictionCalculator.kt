package com.elementary.tasks.reminder.build.logic.builderstate

import androidx.annotation.DrawableRes
import com.elementary.tasks.R
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Reminder
import com.github.naz013.icalendar.ICalendarApi

class ReminderPredictionCalculator(
  private val dateTimeManager: DateTimeManager,
  private val iCalendarApi: ICalendarApi,
  private val textProvider: TextProvider
) {

  operator fun invoke(reminder: Reminder): ReminderPrediction {
    val type = reminder.readType()

    return when {
      type.isGpsType() -> {
        if (reminder.places.isEmpty()) {
          return ReminderPrediction.FailedPrediction(
            icon = R.drawable.ic_fluent_location_not_found,
            message = textProvider.getString(R.string.builder_error_no_places)
          )
        }
        if (reminder.hasReminder && reminder.eventTime.isNotEmpty()) {
          val dateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
            ?: return ReminderPrediction.FailedPrediction(
              icon = R.drawable.ic_fluent_error_circle,
              message = textProvider.getString(R.string.builder_error_cannot_parse_date_time)
            )
          ReminderPrediction.SuccessPrediction(
            icon = R.drawable.ic_builder_forecast,
            message = textProvider.getString(R.string.builder_delayed_tracking_until) +
              dateTimeManager.getDateTime(dateTime)
          )
        } else {
          ReminderPrediction.SuccessPrediction(
            icon = R.drawable.ic_builder_forecast,
            message = textProvider.getString(R.string.builder_will_start_tracking_immediately)
          )
        }
      }

      type.hasSubTasks() && !reminder.hasReminder -> {
        ReminderPrediction.SuccessPrediction(
          icon = R.drawable.ic_builder_forecast,
          message = textProvider.getString(R.string.builder_permanent_reminder_with_sub_tasks)
        )
      }

      else -> {
        if (reminder.eventTime.isEmpty()) {
          ReminderPrediction.FailedPrediction(
            icon = R.drawable.ic_fluent_warning,
            message = textProvider.getString(R.string.builder_error_no_event_time)
          )
        } else {
          val dateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
            ?: return ReminderPrediction.FailedPrediction(
              icon = R.drawable.ic_fluent_error_circle,
              message = textProvider.getString(R.string.builder_error_cannot_parse_date_time)
            )

          if (dateTime.isBefore(dateTimeManager.getCurrentDateTime())) {
            ReminderPrediction.SuccessPrediction(
              icon = R.drawable.ic_builder_forecast,
              message = textProvider.getString(
                R.string.builder_will_trigger_immediately_because_before_the_now_time
              )
            )
          } else {
            ReminderPrediction.SuccessPrediction(
              icon = R.drawable.ic_builder_forecast,
              message = textProvider.getString(R.string.builder_scheduled_at) +
                dateTimeManager.getDateTime(dateTime) +
                createRecurrenceMessage(reminder)
            )
          }
        }
      }
    }
  }

  private fun createRecurrenceMessage(reminder: Reminder): String {
    return if (reminder.readType().isICalendar()) {
      val rules = reminder.recurDataObject?.let {
        runCatching { iCalendarApi.parseObject(it) }.getOrNull()
      }
      if (rules == null) {
        return ""
      }

      val recurrence = runCatching { iCalendarApi.generate(rules) }.getOrNull()
      if (recurrence == null) {
        return ""
      }

      val nowDateTime = dateTimeManager.getCurrentDateTime().withNano(0)

      var nowSelected = false
      var position = -1

      recurrence.forEachIndexed { index, utcDateTime ->
        val dateTime = utcDateTime.dateTime
        if (dateTime != null) {
          if (!nowSelected) {
            if (dateTime.isEqual(nowDateTime) || dateTime.isAfter(nowDateTime)) {
              position = index
              nowSelected = true
            }
          }
        }
      }

      if (nowSelected && position != -1) {
        val afterRecurrences = recurrence.subList(position, recurrence.size)
          .mapNotNull { it.dateTime }
        if (afterRecurrences.isNotEmpty()) {
          textProvider.getString(R.string.and_will_be_repeated_at) +
            afterRecurrences.joinToString(",\n") { dateTimeManager.getDateTime(it) }
        } else {
          ""
        }
      } else {
        ""
      }
    } else {
      ""
    }
  }
}

sealed class ReminderPrediction {
  data class SuccessPrediction(
    @DrawableRes
    val icon: Int,
    val message: String
  ) : ReminderPrediction()

  data class FailedPrediction(
    @DrawableRes
    val icon: Int,
    val message: String
  ) : ReminderPrediction()
}
