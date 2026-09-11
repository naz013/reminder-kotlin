package com.elementary.tasks.core.services.usecase

import android.content.Context
import android.location.Location
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.DefaultDistanceFormatter
import com.github.naz013.logic.workflow.WorkflowTriggerRunner
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.usecase.StopLocationTrackingUseCase
import com.github.naz013.repository.ReminderV2Repository

class CheckLocationReminderUseCase(
  context: Context,
  private val reminderV2Repository: ReminderV2Repository,
  private val dateTimeManager: DateTimeManager,
  private val workflowTriggerRunner: WorkflowTriggerRunner,
  prefs: Prefs,
  private val stopLocationTrackingUseCase: StopLocationTrackingUseCase,
  private val placeDistanceCalculator: PlaceDistanceCalculator,
) {
  private val distanceFormatter: DefaultDistanceFormatter =
    DefaultDistanceFormatter(
      context = context,
      useMetric = prefs.useMetric,
    )
  private val stockRadius: Int = prefs.radius
  private val isNotificationEnabled: Boolean = prefs.isDistanceNotificationEnabled

  suspend operator fun invoke(location: Location): Result {
    val showDistanceNotifications = mutableListOf<ShowDistanceNotification>()
    val showReminderNotifications = mutableListOf<ShowReminderNotification>()

    val gpsReminders = reminderV2Repository.getAll(active = true, removed = false).filter { it.places.isNotEmpty() }
    for (reminder in gpsReminders) {
      if (reminder.location?.isNotificationShown == true) continue
      if (shouldCheckDistance(reminder)) {
        when {
          destinationReached(location, reminder) -> {
            val firedReminder =
              reminder.copy(location = (reminder.location ?: LocationSettings()).copy(isNotificationShown = true))
            reminderV2Repository.save(firedReminder)
            showReminderNotifications.add(ShowReminderNotification(reminder.uuId))
            if (reminder.isLeavingType()) {
              workflowTriggerRunner.onLocationExited(reminder.uuId)
            } else {
              workflowTriggerRunner.onLocationEntered(reminder.uuId)
            }
            // Notification for this reminder is done firing; wind the service down immediately
            // if no other GPS reminder is still pending, instead of waiting for the user to tap
            // "complete" on the notification.
            stopLocationTrackingUseCase(reminder = firedReminder, isPaused = false)
          }

          shouldShowDistanceNotification(reminder) -> {
            showDistanceNotifications.add(
              ShowDistanceNotification(
                uniqueId = reminder.uniqueId,
                title = reminder.summary,
                text = getDistanceText(location, reminder),
              ),
            )
          }

          shouldLockReminder(location, reminder) -> {
            reminderV2Repository.save(
              reminder.copy(location = (reminder.location ?: LocationSettings()).copy(isLocked = true)),
            )
          }
        }
      }
    }

    return Result(showDistanceNotifications, showReminderNotifications)
  }

  private fun getDistanceText(
    location: Location,
    reminder: ReminderV2,
  ): String = distanceFormatter.format(getDistance(location, reminder))

  private fun shouldLockReminder(
    location: Location,
    reminder: ReminderV2,
  ): Boolean =
    if (reminder.isLeavingType()) {
      val distance = getDistance(location, reminder)
      val place = reminder.places[0]
      reminder.location?.isLocked != true && distance < getRadius(place.radius)
    } else {
      false
    }

  private fun shouldShowDistanceNotification(reminder: ReminderV2): Boolean {
    if (!isNotificationEnabled) return false
    return if (reminder.isLeavingType()) {
      reminder.location?.isLocked == true
    } else {
      true
    }
  }

  private fun destinationReached(
    location: Location,
    reminder: ReminderV2,
  ): Boolean {
    val distance = getDistance(location, reminder)
    val place = reminder.places[0]
    return if (reminder.isLeavingType()) {
      reminder.location?.isLocked == true && distance > getRadius(place.radius)
    } else {
      distance <= getRadius(place.radius)
    }
  }

  private fun getRadius(r: Int): Int {
    var radius = r
    if (radius == -1) radius = stockRadius
    return radius
  }

  private fun getDistance(
    location: Location,
    reminder: ReminderV2,
  ): Int = placeDistanceCalculator.metersTo(location, reminder.places[0])

  private fun shouldCheckDistance(reminder: ReminderV2): Boolean {
    // eventDateTime on a GPS reminder is the "delayed reminder" start threshold (see
    // RecurrenceRuleCalculator.fromLocation()); isCurrent() is true while that threshold is
    // still upcoming, so distance checks should only start once it's no longer current.
    val eventDateTime = reminder.schedule.eventDateTime ?: return true
    return !dateTimeManager.isCurrent(dateTimeManager.utcToLocal(eventDateTime))
  }

  private fun ReminderV2.isLeavingType(): Boolean = recurrence is RecurrenceRule.LocationExit

  data class Result(
    val showDistanceNotifications: List<ShowDistanceNotification>,
    val showReminderNotifications: List<ShowReminderNotification>,
  )

  data class ShowReminderNotification(
    val uuId: String,
  )

  data class ShowDistanceNotification(
    val uniqueId: Int,
    val title: String,
    val text: String,
  )
}
