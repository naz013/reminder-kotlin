package com.elementary.tasks.core.services.usecase

import android.content.Context
import android.location.Location
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.DefaultDistanceFormatter
import com.github.naz013.domain.Reminder
import com.github.naz013.repository.ReminderRepository
import kotlin.math.roundToInt

class CheckLocationReminderUseCase(
  context: Context,
  private val reminderRepository: ReminderRepository,
  private val dateTimeManager: DateTimeManager,
  prefs: Prefs
) {

  private val distanceFormatter: DefaultDistanceFormatter = DefaultDistanceFormatter(
    context = context,
    useMetric = prefs.useMetric
  )
  private val stockRadius: Int = prefs.radius
  private val isNotificationEnabled: Boolean = prefs.isDistanceNotificationEnabled

  suspend operator fun invoke(location: Location): Result {
    val showDistanceNotifications = mutableListOf<ShowDistanceNotification>()
    val showReminderNotifications = mutableListOf<ShowReminderNotification>()

    for (reminder in reminderRepository.getActiveGpsTypes()) {
      if (reminder.isNotificationShown) continue
      if (shouldCheckDistance(reminder)) {
        when {
          destinationReached(location, reminder) -> {
            reminder.isNotificationShown = true
            reminderRepository.save(reminder)
            showReminderNotifications.add(ShowReminderNotification(reminder.uuId))
          }

          shouldShowDistanceNotification(reminder) -> {
            showDistanceNotifications.add(
              ShowDistanceNotification(
                uniqueId = reminder.uniqueId,
                title = reminder.summary,
                text = getDistanceText(location, reminder)
              )
            )
          }

          shouldLockReminder(location, reminder) -> {
            reminder.isLocked = true
            reminderRepository.save(reminder)
          }
        }
      }
    }

    return Result(showDistanceNotifications, showReminderNotifications)
  }

  private fun getDistanceText(location: Location, reminder: Reminder): String {
    return distanceFormatter.format(getDistance(location, reminder))
  }

  private fun shouldLockReminder(location: Location, reminder: Reminder): Boolean {
    return if (reminder.isLeavingType()) {
      val distance = getDistance(location, reminder)
      val place = reminder.places[0]
      !reminder.isLocked && distance < getRadius(place.radius)
    } else {
      false
    }
  }

  private fun shouldShowDistanceNotification(reminder: Reminder): Boolean {
    if (!isNotificationEnabled) return false
    return if (reminder.isLeavingType()) {
      reminder.isLocked
    } else {
      true
    }
  }

  private fun destinationReached(location: Location, reminder: Reminder): Boolean {
    val distance = getDistance(location, reminder)
    val place = reminder.places[0]
    return if (reminder.isLeavingType()) {
      reminder.isLocked && distance > getRadius(place.radius)
    } else {
      distance <= getRadius(place.radius)
    }
  }

  private fun getRadius(r: Int): Int {
    var radius = r
    if (radius == -1) radius = stockRadius
    return radius
  }

  private fun getDistance(location: Location, reminder: Reminder): Int {
    return if (reminder.isLeavingType()) {
      val place = reminder.places[0]
      val loc = Location("point B").apply {
        latitude = place.latitude
        longitude = place.longitude
      }

      val distance = location.distanceTo(loc)
      distance.roundToInt()
    } else {
      val place = reminder.places[0]
      val loc = Location("point B").apply {
        latitude = place.latitude
        longitude = place.longitude
      }

      val distance = location.distanceTo(loc)
      distance.roundToInt()
    }
  }

  private fun shouldCheckDistance(reminder: Reminder): Boolean {
    return if (reminder.eventTime.isEmpty()) {
      true
    } else {
      dateTimeManager.isCurrent(reminder.eventTime)
    }
  }

  private fun Reminder.isLeavingType(): Boolean {
    return Reminder.isBase(type, Reminder.BY_OUT)
  }

  data class Result(
    val showDistanceNotifications: List<ShowDistanceNotification>,
    val showReminderNotifications: List<ShowReminderNotification>
  )

  data class ShowReminderNotification(
    val uuId: String
  )

  data class ShowDistanceNotification(
    val uniqueId: Int,
    val title: String,
    val text: String
  )
}
