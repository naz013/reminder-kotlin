package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.utils.EnableThread
import com.elementary.tasks.core.utils.Notifier
import org.koin.core.component.get
import timber.log.Timber

class BootReceiver : BaseBroadcast() {

  override fun onReceive(context: Context, intent: Intent) {
    Timber.d("onReceive: ")
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      get<EnableThread>().run()
      if (prefs.isBirthdayReminderEnabled) {
        EventJobScheduler.scheduleDailyBirthday(prefs)
      }
      if (prefs.isSbNotificationEnabled) {
        Notifier.updateReminderPermanent(context, PermanentReminderReceiver.ACTION_SHOW)
      }
      if (prefs.isContactAutoCheckEnabled) {
        EventJobScheduler.scheduleBirthdaysCheck(context)
      }
      if (prefs.isAutoEventsCheckEnabled) {
        EventJobScheduler.scheduleEventCheck(prefs)
      }
      if (prefs.isBackupEnabled) {
        EventJobScheduler.scheduleAutoBackup(prefs)
        EventJobScheduler.scheduleAutoSync(prefs)
      }
      if (prefs.isBirthdayPermanentEnabled) {
        EventJobScheduler.scheduleBirthdayPermanent()
        Notifier.showBirthdayPermanent(context, prefs)
      }
    }
  }
}
