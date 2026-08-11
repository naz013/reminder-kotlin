package com.elementary.tasks.core.services.usecase

import android.content.Context
import android.location.Location
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.DefaultDistanceFormatter
import com.github.naz013.feature.workflow.WorkflowTriggerRunner
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import kotlin.math.roundToInt

class CheckLocationReminderUseCase(
  context: Context,
  private val reminderV2Repository: ReminderV2Repository,
  private val dateTimeManager: DateTimeManager,
  private val workflowTriggerRunner: WorkflowTriggerRunner,
  prefs: Prefs,
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
            reminderV2Repository.save(
              reminder.copy(location = (reminder.location ?: LocationSettings()).copy(isNotificationShown = true)),
            )
            showReminderNotifications.add(ShowReminderNotification(reminder.uuId))
            if (reminder.isLeavingType()) {
              workflowTriggerRunner.onLocationExited(reminder.uuId)
            } else {
              workflowTriggerRunner.onLocationEntered(reminder.uuId)
            }
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
  ): Int =
    if (reminder.isLeavingType()) {
      val place = reminder.places[0]
      val loc =
        Location("point B").apply {
          latitude = place.latitude
          longitude = place.longitude
        }

      val distance = location.distanceTo(loc)
      distance.roundToInt()
    } else {
      val place = reminder.places[0]
      val loc =
        Location("point B").apply {
          latitude = place.latitude
          longitude = place.longitude
        }

      val distance = location.distanceTo(loc)
      distance.roundToInt()
    }

  private fun shouldCheckDistance(reminder: ReminderV2): Boolean {
    val eventDateTime = reminder.schedule.eventDateTime ?: return true
    return dateTimeManager.isCurrent(dateTimeManager.utcToLocal(eventDateTime))
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
