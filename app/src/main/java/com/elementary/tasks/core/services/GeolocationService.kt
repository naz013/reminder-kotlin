package com.elementary.tasks.core.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.services.action.reminder.ReminderActionProcessor
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.DefaultDistanceFormatter
import com.elementary.tasks.core.utils.withUIContext
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import kotlin.math.roundToInt

class GeolocationService : Service() {

  private var isNotificationEnabled: Boolean = false
  private var stockRadius: Int = 0

  private val prefs by inject<Prefs>()
  private val reminderDao by inject<ReminderDao>()
  private val notifier by inject<Notifier>()
  private val dateTimeManager by inject<DateTimeManager>()
  private val reminderActionProcessor by inject<ReminderActionProcessor>()
  private val distanceFormatter = DefaultDistanceFormatter(this, prefs.useMetric)

  private val locationTracker by inject<LocationTracker> { parametersOf(locationListener) }
  private var locationListener: LocationTracker.Listener = object : LocationTracker.Listener {
    override fun onUpdate(lat: Double, lng: Double) {
      val locationA = Location("point A")
      locationA.latitude = lat
      locationA.longitude = lng
      checkReminders(locationA)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    locationTracker.removeUpdates()
    stopForeground(STOP_FOREGROUND_REMOVE)
    Timber.d("onDestroy: ")
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onCreate() {
    super.onCreate()
    showDefaultNotification()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Timber.d("onStartCommand: ")
    showDefaultNotification()
    isNotificationEnabled = prefs.isDistanceNotificationEnabled
    stockRadius = prefs.radius
    locationTracker.startUpdates()
    return START_STICKY
  }

  private fun checkReminders(locationA: Location) {
    launchDefault {
      for (reminder in reminderDao.getAll(active = true, removed = false)) {
        if (Reminder.isGpsType(reminder.type)) {
          checkDistance(locationA, reminder)
        }
      }
    }
  }

  private suspend fun checkDistance(locationA: Location, reminder: Reminder) {
    if (!TextUtils.isEmpty(reminder.eventTime)) {
      if (dateTimeManager.isCurrent(reminder.eventTime)) {
        selectBranch(locationA, reminder)
      }
    } else {
      selectBranch(locationA, reminder)
    }
  }

  private suspend fun selectBranch(locationA: Location, reminder: Reminder) {
    if (reminder.isNotificationShown) return
    when {
      Reminder.isBase(reminder.type, Reminder.BY_OUT) -> checkOut(locationA, reminder)
      else -> checkSimple(locationA, reminder)
    }
  }

  private suspend fun checkSimple(locationA: Location, reminder: Reminder) {
    val place = reminder.places[0]
    val locationB = Location("point B")
    locationB.latitude = place.latitude
    locationB.longitude = place.longitude
    val distance = locationA.distanceTo(locationB)
    val roundedDistance = distance.roundToInt()
    if (roundedDistance <= getRadius(place.radius)) {
      showReminder(reminder)
    } else {
      showNotification(roundedDistance, reminder)
    }
  }

  private fun getRadius(r: Int): Int {
    var radius = r
    if (radius == -1) radius = stockRadius
    return radius
  }

  private suspend fun checkOut(locationA: Location, reminder: Reminder) {
    val place = reminder.places[0]
    val locationB = Location("point B")
    locationB.latitude = place.latitude
    locationB.longitude = place.longitude
    val distance = locationA.distanceTo(locationB)
    val roundedDistance = distance.roundToInt()
    if (reminder.isLocked) {
      if (roundedDistance > getRadius(place.radius)) {
        showReminder(reminder)
      } else {
        if (isNotificationEnabled) {
          showNotification(roundedDistance, reminder)
        }
      }
    } else {
      if (roundedDistance < getRadius(place.radius)) {
        reminder.isLocked = true
        reminderDao.insert(reminder)
      }
    }
  }

  private fun reminderAction(id: String) {
    reminderActionProcessor.process(id)
  }

  private suspend fun showReminder(reminder: Reminder) {
    if (reminder.isNotificationShown) return
    reminder.isNotificationShown = true
    reminderDao.insert(reminder)
    withUIContext { reminderAction(reminder.uuId) }
  }

  private fun showNotification(roundedDistance: Int, reminder: Reminder) {
    if (!isNotificationEnabled) return
    val builder = NotificationCompat.Builder(applicationContext, Notifier.CHANNEL_SILENT)
    builder.setContentTitle(reminder.summary)
    builder.setContentText(distanceFormatter.format(roundedDistance))
    builder.priority = NotificationCompat.PRIORITY_MIN
    builder.setSmallIcon(R.drawable.ic_builder_map_my_location)
    builder.setCategory(NotificationCompat.CATEGORY_NAVIGATION)
    notifier.notify(reminder.uniqueId, builder.build())
  }

  private fun showDefaultNotification() {
    val builder = NotificationCompat.Builder(applicationContext, Notifier.CHANNEL_SYSTEM)
    if (Module.isPro) {
      builder.setContentText(getString(R.string.app_name_pro))
    } else {
      builder.setContentText(getString(R.string.app_name))
    }
    builder.setContentTitle(getString(R.string.location_tracking_service_running))
    builder.setSmallIcon(R.drawable.ic_builder_map_my_location)
    startForeground(NOTIFICATION_ID, builder.build())
  }

  companion object {
    private const val NOTIFICATION_ID = 1245
  }
}
