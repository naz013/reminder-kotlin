package com.elementary.tasks.splash

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.EnableThread
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.groups.GroupsUtil

class SplashViewModel(
  gTasks: GTasks,
  private val appDb: AppDb,
  private val prefs: Prefs,
  private val context: Context,
  private val enableThread: EnableThread
) : ViewModel(), LifecycleObserver {

  val isGoogleTasksEnabled = gTasks.isLogged
  val openHome = mutableLiveDataOf<Boolean>()

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  fun onResume() {
    launchDefault {
      initPrefs()
      checkIfAppUpdated()
      checkDb()
      withUIContext {
        if (prefs.isSbNotificationEnabled) {
          Notifier.updateReminderPermanent(context, PermanentReminderReceiver.ACTION_SHOW)
        }
        openHome.postValue(prefs.hasPinCode)
      }
    }
  }

  private fun checkDb() {
    try {
      if (appDb.reminderGroupDao().all().isEmpty()) {
        GroupsUtil.initDefault(context, appDb)
      }
    } catch (e: Exception) {
    }
  }

  private fun checkIfAppUpdated() {
    try {
      val info = context.packageManager.getPackageInfo(context.packageName, 0)
      if (!prefs.getVersion(info.versionName)) {
        prefs.saveVersionBoolean(info.versionName)
        enableThread.run()
      }
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
  }

  private fun initPrefs() {
    prefs.initPrefs(context)
    prefs.checkPrefs()
  }
}