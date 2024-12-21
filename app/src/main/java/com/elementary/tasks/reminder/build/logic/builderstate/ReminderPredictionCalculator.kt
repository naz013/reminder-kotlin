package com.elementary.tasks.reminder.build.logic.builderstate

import androidx.annotation.DrawableRes
import com.elementary.tasks.R
import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager

class ReminderPredictionCalculator(
  private val dateTimeManager: DateTimeManager,
  contextProvider: ContextProvider
) {

  private val context = contextProvider.context

  operator fun invoke(reminder: Reminder): ReminderPrediction {
    val type = UiReminderType(reminder.type)

    return when {
      type.isGpsType() -> {
        if (reminder.places.isEmpty()) {
          return ReminderPrediction.FailedPrediction(
            icon = R.drawable.ic_fluent_location_not_found,
            message = context.getString(R.string.builder_error_no_places)
          )
        }
        if (reminder.hasReminder && reminder.eventTime.isNotEmpty()) {
          val dateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
            ?: return ReminderPrediction.FailedPrediction(
              icon = R.drawable.ic_fluent_error_circle,
              message = context.getString(R.string.builder_error_cannot_parse_date_time)
            )
          ReminderPrediction.SuccessPrediction(
            icon = R.drawable.ic_builder_forecast,
            message = context.getString(R.string.builder_delayed_tracking_until) +
              dateTimeManager.getDateTime(dateTime)
          )
        } else {
          ReminderPrediction.SuccessPrediction(
            icon = R.drawable.ic_builder_forecast,
            message = context.getString(R.string.builder_will_start_tracking_immediately)
          )
        }
      }

      type.isSubTasks() && !reminder.hasReminder -> {
        ReminderPrediction.SuccessPrediction(
          icon = R.drawable.ic_builder_forecast,
          message = context.getString(R.string.builder_permanent_reminder_with_sub_tasks)
        )
      }

      else -> {
        if (reminder.eventTime.isEmpty()) {
          ReminderPrediction.FailedPrediction(
            icon = R.drawable.ic_fluent_warning,
            message = context.getString(R.string.builder_error_no_event_time)
          )
        } else {
          val dateTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
            ?: return ReminderPrediction.FailedPrediction(
              icon = R.drawable.ic_fluent_error_circle,
              message = context.getString(R.string.builder_error_cannot_parse_date_time)
            )

          if (dateTime.isBefore(dateTimeManager.getCurrentDateTime())) {
            ReminderPrediction.SuccessPrediction(
              icon = R.drawable.ic_builder_forecast,
              message = context.getString(
                R.string.builder_will_trigger_immediately_because_before_the_now_time
              )
            )
          } else {
            ReminderPrediction.SuccessPrediction(
              icon = R.drawable.ic_builder_forecast,
              message = context.getString(R.string.builder_scheduled_at) +
                dateTimeManager.getDateTime(dateTime)
            )
          }
        }
      }
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
