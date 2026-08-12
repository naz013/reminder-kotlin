package com.github.naz013.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionPermissionRequiredException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appfunctions.reminder.CompleteReminderUseCase
import com.github.naz013.appfunctions.reminder.CreateReminderParams
import com.github.naz013.appfunctions.reminder.CreateSimpleReminderUseCase
import com.github.naz013.appfunctions.reminder.DeleteReminderUseCase
import com.github.naz013.appfunctions.reminder.ListUpcomingRemindersParams
import com.github.naz013.appfunctions.reminder.ListUpcomingRemindersUseCase
import com.github.naz013.appfunctions.reminder.ReminderFunctionResult
import com.github.naz013.appfunctions.reminder.ReminderIdParams
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.domain.reminder.v2.ReminderV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Exposes reminder-related capabilities of the app to Gemini and other on-device assistants via
 * the AppFunctions platform API. Only available in the PRO build - see [BuildInfo.isPro].
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
  serviceName = "ReminderAppFunctionService",
  appFunctionXmlFileName = "reminder_app_function_service",
)
abstract class BaseReminderAppFunctionService :
  AppFunctionService(),
  KoinComponent {
  private val buildInfo: BuildInfo by inject()
  private val dateTimeManager: DateTimeManager by inject()
  private val createSimpleReminderUseCase: CreateSimpleReminderUseCase by inject()
  private val listUpcomingRemindersUseCase: ListUpcomingRemindersUseCase by inject()
  private val completeReminderUseCase: CompleteReminderUseCase by inject()
  private val deleteReminderUseCase: DeleteReminderUseCase by inject()
  private val analyticsEventSender: AnalyticsEventSender by inject()

  /**
   * Creates a new one-time reminder.
   *
   * @param params The title, due date/time, and optional notes for the reminder.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun createReminder(params: CreateReminderParams): ReminderFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()
      if (params.title.isBlank()) {
        throw AppFunctionInvalidArgumentException("Title must not be blank")
      }

      val reminder =
        createSimpleReminderUseCase(
          title = params.title,
          dueDateTime = params.dueDateTime,
          notes = params.notes,
        )

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_CREATE_REMINDER))

      reminder.toFunctionResult()
    }

  /**
   * Lists active reminders due within the next [ListUpcomingRemindersParams.withinDays] days,
   * soonest first.
   *
   * @param params How many days from now to look for due reminders in.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun listUpcomingReminders(params: ListUpcomingRemindersParams): List<ReminderFunctionResult> =
    withContext(Dispatchers.IO) {
      requirePro()
      if (params.withinDays < 0) {
        throw AppFunctionInvalidArgumentException("withinDays must not be negative")
      }

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_LIST_REMINDERS))

      listUpcomingRemindersUseCase(params.withinDays).map { it.toFunctionResult() }
    }

  /**
   * Marks a reminder as complete, so it no longer triggers.
   *
   * @param params The id of the reminder to complete.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun completeReminder(params: ReminderIdParams): ReminderFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()

      val reminder =
        completeReminderUseCase(params.id)
          ?: throw AppFunctionElementNotFoundException("No reminder found with id = ${params.id}")

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_COMPLETE_REMINDER))

      reminder.toFunctionResult()
    }

  /**
   * Permanently deletes a reminder.
   *
   * @param params The id of the reminder to delete.
   */
  @AppFunction(isDescribedByKDoc = true)
  internal suspend fun deleteReminder(params: ReminderIdParams): ReminderFunctionResult =
    withContext(Dispatchers.IO) {
      requirePro()

      val reminder =
        deleteReminderUseCase(params.id)
          ?: throw AppFunctionElementNotFoundException("No reminder found with id = ${params.id}")

      analyticsEventSender.send(FeatureUsedEvent(Feature.APP_FUNCTION_DELETE_REMINDER))

      reminder.toFunctionResult()
    }

  private fun requirePro() {
    if (!buildInfo.isPro) {
      throw AppFunctionPermissionRequiredException(
        "Reminder AppFunctions require the PRO version of the app.",
      )
    }
  }

  private fun ReminderV2.toFunctionResult(): ReminderFunctionResult {
    val utcDateTime = schedule.eventDateTime ?: schedule.startDateTime
    return ReminderFunctionResult(
      id = uuId,
      title = summary,
      dueDateTime = dateTimeManager.utcToLocal(utcDateTime).toJavaTime(),
    )
  }
}
