package com.elementary.tasks.core.services

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.services.action.reminder.ReminderActionProcessor
import com.elementary.tasks.core.services.usecase.CheckLocationReminderUseCase
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.github.naz013.logging.Logger
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class GeolocationService : Service() {

  private val notifier by inject<Notifier>()
  private val reminderActionProcessor by inject<ReminderActionProcessor>()
  private val checkLocationReminderUseCase by inject<CheckLocationReminderUseCase>()

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
    Logger.d("onDestroy: ")
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onCreate() {
    super.onCreate()
    showDefaultNotification()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Logger.d("onStartCommand: ")
    showDefaultNotification()
    locationTracker.startUpdates()
    return START_STICKY
  }

  private fun checkReminders(location: Location) {
    launchDefault {
      val result = checkLocationReminderUseCase(location)
      result.showDistanceNotifications.forEach { showNotification(it) }
      result.showReminderNotifications.forEach { showReminder(it) }
    }
  }

  private fun reminderAction(id: String) {
    reminderActionProcessor.process(id)
  }

  private suspend fun showReminder(
    showReminderNotification: CheckLocationReminderUseCase.ShowReminderNotification
  ) {
    withUIContext { reminderAction(showReminderNotification.uuId) }
  }

  private fun showNotification(
    notification: CheckLocationReminderUseCase.ShowDistanceNotification
  ) {
    val builder = NotificationCompat.Builder(applicationContext, Notifier.CHANNEL_SILENT)
    builder.setContentTitle(notification.title)
    builder.setContentText(notification.text)
    builder.priority = NotificationCompat.PRIORITY_MIN
    builder.setSmallIcon(R.drawable.ic_builder_map_my_location)
    builder.setCategory(NotificationCompat.CATEGORY_NAVIGATION)
    notifier.notify(notification.uniqueId, builder.build())
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      try {
        startForeground(NOTIFICATION_ID, builder.build())
        Logger.i("GeolocationService", "Start foreground: success")
      } catch (e: ForegroundServiceStartNotAllowedException) {
        Logger.i("GeolocationService", "Start foreground: not allowed")
        stopSelf()
      }
    } else {
      startForeground(NOTIFICATION_ID, builder.build())
      Logger.i("GeolocationService", "Start foreground: success")
    }
  }

  companion object {
    private const val NOTIFICATION_ID = 1245
  }
}
