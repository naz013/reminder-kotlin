package com.elementary.tasks.reminder.scheduling

import android.content.Context
import com.elementary.tasks.core.controller.EventControl
import com.elementary.tasks.core.controller.LocationEvent
import com.elementary.tasks.core.controller.ShoppingEvent
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.datetime.RecurEventManager
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.ui.common.datetime.ModelDateTimeFormatter

/**
 * Strategy-based factory for creating EventControl instances.
 * Uses BehaviorStrategyResolver to determine behavior without relying on reminderType.
 *
 * This factory analyzes the intrinsic properties of a reminder (such as places,
 * weekdays, repeatInterval, recurDataObject, etc.) to determine which controller
 * and strategy should be used.
 */
class StrategyBasedEventControlFactory(
  private val prefs: Prefs,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val context: Context,
  private val notifier: Notifier,
  private val jobScheduler: JobScheduler,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val textProvider: TextProvider,
  private val reminderRepository: ReminderRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val dateTimeManager: DateTimeManager,
  private val recurEventManager: RecurEventManager,
  private val modelDateTimeFormatter: ModelDateTimeFormatter
) {

  private val strategyResolver = BehaviorStrategyResolver(
    dateTimeManager,
    recurEventManager,
    modelDateTimeFormatter
  )

  /**
   * Creates an EventControl instance for the given reminder.
   * Determines behavior based on Reminder properties, not reminderType.
   *
   * @param reminder The reminder to create a controller for
   * @return EventControl instance with appropriate behavior
   */
  fun getController(reminder: Reminder): EventControl {
    val strategy = strategyResolver.resolve(reminder)

    return when (strategy) {
      is LocationBasedStrategy -> {
        createLocationEventController(reminder)
      }

      else -> {
        // All time-based strategies use unified controller
        createUnifiedTimeBasedController(reminder, strategy)
      }
    }.also {
      Logger.d(
        "StrategyBasedEventControlFactory",
        "getController: ${it::class.simpleName} with strategy ${strategy::class.simpleName}"
      )
    }
  }

  /**
   * Creates a LocationEvent controller for GPS-based reminders.
   *
   * @param reminder The reminder with places
   * @return LocationEvent controller
   */
  private fun createLocationEventController(reminder: Reminder): EventControl {
    return LocationEvent(
      reminder,
      reminderRepository,
      prefs,
      context,
      notifier,
      jobScheduler,
      appWidgetUpdater,
      dateTimeManager
    )
  }

  /**
   * Creates a UnifiedTimeBasedEvent controller with the appropriate strategy.
   *
   * @param reminder The reminder to control
   * @param strategy The behavior strategy to use
   * @return UnifiedTimeBasedEvent controller
   */
  private fun createUnifiedTimeBasedController(
    reminder: Reminder,
    strategy: ReminderBehaviorStrategy
  ): EventControl {
    return UnifiedTimeBasedEvent(
      reminder,
      reminderRepository,
      prefs,
      googleCalendarUtils,
      notifier,
      jobScheduler,
      appWidgetUpdater,
      textProvider,
      dateTimeManager,
      googleTaskRepository,
      strategy
    )
  }
}

