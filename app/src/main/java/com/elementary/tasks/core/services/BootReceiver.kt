package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.utils.EnableThread
import com.github.naz013.logging.Logger
import org.koin.core.component.get
import org.koin.core.component.inject

class BootReceiver : BaseBroadcast() {

  private val jobScheduler by inject<JobScheduler>()

  override fun onReceive(context: Context, intent: Intent) {
    Logger.i("Device boot completed")
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      get<EnableThread>().run()
      if (prefs.isBirthdayReminderEnabled) {
        jobScheduler.scheduleDailyBirthday()
      }
      if (prefs.isSbNotificationEnabled) {
        PermanentReminderReceiver.show(context)
      }
      if (prefs.isContactAutoCheckEnabled) {
        jobScheduler.scheduleBirthdaysCheck()
      }
      if (prefs.isAutoEventsCheckEnabled) {
        jobScheduler.scheduleEventCheck()
      }
      if (prefs.isBackupEnabled) {
        jobScheduler.scheduleAutoBackup()
        jobScheduler.scheduleAutoSync()
      }
      if (prefs.isBirthdayPermanentEnabled) {
        jobScheduler.scheduleBirthdayPermanent()
        notifier.showBirthdayPermanent()
      }
    }
  }
}
