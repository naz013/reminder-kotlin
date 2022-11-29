package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.utils.EnableThread
import org.koin.core.component.get
import timber.log.Timber

class BootReceiver : BaseBroadcast() {

  override fun onReceive(context: Context, intent: Intent) {
    Timber.d("onReceive: ")
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      get<EnableThread>().run()
      if (prefs.isBirthdayReminderEnabled) {
        JobScheduler.scheduleDailyBirthday(prefs)
      }
      if (prefs.isSbNotificationEnabled) {
        notifier.updateReminderPermanent(PermanentReminderReceiver.ACTION_SHOW)
      }
      if (prefs.isContactAutoCheckEnabled) {
        JobScheduler.scheduleBirthdaysCheck(context)
      }
      if (prefs.isAutoEventsCheckEnabled) {
        JobScheduler.scheduleEventCheck(prefs)
      }
      if (prefs.isBackupEnabled) {
        JobScheduler.scheduleAutoBackup(prefs)
        JobScheduler.scheduleAutoSync(prefs)
      }
      if (prefs.isBirthdayPermanentEnabled) {
        JobScheduler.scheduleBirthdayPermanent()
        notifier.showBirthdayPermanent()
      }
    }
  }
}
