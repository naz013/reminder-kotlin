package com.github.naz013.feature.reminder.build.logic.builderstate

import androidx.annotation.DrawableRes
import com.github.naz013.ui.common.R
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.icalendar.ICalendarApi

class ReminderPredictionCalculator(
  private val dateTimeManager: DateTimeManager,
  private val iCalendarApi: ICalendarApi,
  private val textProvider: TextProvider,
) {
  operator fun invoke(reminder: ReminderV2): ReminderPrediction {
    val recurrence = reminder.recurrence

    return when {
      recurrence == RecurrenceRule.LocationEnter || recurrence == RecurrenceRule.LocationExit -> {
        if (reminder.places.isEmpty()) {
          return ReminderPrediction.FailedPrediction(
            icon = R.drawable.ic_fluent_location_not_found,
            message = textProvider.getString(R.string.builder_error_no_places),
          )
        }
        val eventDateTime = reminder.schedule.eventDateTime
        if (reminder.location?.hasDelayedReminder == true && eventDateTime != null) {
          val dateTime = dateTimeManager.utcToLocal(eventDateTime)
          ReminderPrediction.SuccessPrediction(
            icon = R.drawable.ic_builder_forecast,
            message =
              textProvider.getString(R.string.builder_delayed_tracking_until) +
                dateTimeManager.getDateTime(dateTime),
          )
        } else {
          ReminderPrediction.SuccessPrediction(
            icon = R.drawable.ic_builder_forecast,
            message = textProvider.getString(R.string.builder_will_start_tracking_immediately),
          )
        }
      }

      reminder.action is ReminderAction.Shopping && reminder.schedule.eventDateTime == null -> {
        ReminderPrediction.SuccessPrediction(
          icon = R.drawable.ic_builder_forecast,
          message = textProvider.getString(R.string.builder_permanent_reminder_with_sub_tasks),
        )
      }

      else -> {
        val eventDateTime = reminder.schedule.eventDateTime
        if (eventDateTime == null) {
          ReminderPrediction.FailedPrediction(
            icon = R.drawable.ic_fluent_warning,
            message = textProvider.getString(R.string.builder_error_no_event_time),
          )
        } else {
          val dateTime = dateTimeManager.utcToLocal(eventDateTime)

          if (dateTime.isBefore(dateTimeManager.getCurrentDateTime())) {
            ReminderPrediction.SuccessPrediction(
              icon = R.drawable.ic_builder_forecast,
              message =
                textProvider.getString(
                  R.string.builder_will_trigger_immediately_because_before_the_now_time,
                ),
            )
          } else {
            ReminderPrediction.SuccessPrediction(
              icon = R.drawable.ic_builder_forecast,
              message =
                textProvider.getString(R.string.builder_scheduled_at) +
                  dateTimeManager.getDateTime(dateTime) +
                  createRecurrenceMessage(recurrence),
            )
          }
        }
      }
    }
  }

  private fun createRecurrenceMessage(recurrence: RecurrenceRule): String {
    val rrule = (recurrence as? RecurrenceRule.ICalendar)?.rrule ?: return ""

    val rules = runCatching { iCalendarApi.parseObject(rrule) }.getOrNull() ?: return ""
    val generated = runCatching { iCalendarApi.generate(rules) }.getOrNull() ?: return ""

    val nowDateTime = dateTimeManager.getCurrentDateTime().withNano(0)

    var nowSelected = false
    var position = -1

    generated.forEachIndexed { index, utcDateTime ->
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

    return if (nowSelected && position != -1) {
      val afterRecurrences =
        generated
          .subList(position, generated.size)
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
  }
}

sealed class ReminderPrediction {
  data class SuccessPrediction(
    @DrawableRes
    val icon: Int,
    val message: String,
  ) : ReminderPrediction()

  data class FailedPrediction(
    @DrawableRes
    val icon: Int,
    val message: String,
  ) : ReminderPrediction()
}
