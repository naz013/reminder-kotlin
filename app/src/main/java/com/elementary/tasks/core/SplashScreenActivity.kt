package com.elementary.tasks.core

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.ThemedActivity
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.services.PermanentReminderReceiver
import com.elementary.tasks.core.utils.EnableThread
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.home.BottomNavActivity
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.groups.GroupsUtil
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class SplashScreenActivity : ThemedActivity() {

  private val db by inject<AppDb>()
  private val gTasks by inject<GTasks>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initPrefs()
    if (prefs.isSbNotificationEnabled) {
      Notifier.updateReminderPermanent(this, PermanentReminderReceiver.ACTION_SHOW)
    }
  }

  private fun checkIfAppUpdated() {
    try {
      val info = packageManager.getPackageInfo(packageName, 0)
      if (!prefs.getVersion(info.versionName)) {
        prefs.saveVersionBoolean(info.versionName)
        get<EnableThread>().run()
      }
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
  }

  override fun onResume() {
    super.onResume()
    gotoApp()
  }

  private fun gotoApp() {
    checkIfAppUpdated()
    launchDefault {
      try {
        if (db.reminderGroupDao().all().isEmpty()) {
          GroupsUtil.initDefault(this@SplashScreenActivity)
        }
      } catch (e: Exception) {
      }
      withUIContext {
        enableShortcuts()
        if (prefs.hasPinCode) {
          openPinLogin()
        } else {
          runApplication()
        }
      }
    }
  }

  private fun openPinLogin() {
    startActivity(Intent(this, PinLoginActivity::class.java))
    finish()
  }

  private fun initPrefs() {
    prefs.initPrefs(this)
    prefs.checkPrefs()
  }

  private fun runApplication() {
    startActivity(Intent(this, BottomNavActivity::class.java))
    finish()
  }

  private fun enableShortcuts() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
      val shortcutManager = getSystemService(ShortcutManager::class.java)
      if (shortcutManager != null) {
        val shortcut = ShortcutInfo.Builder(this, "id.reminder")
          .setShortLabel(getString(R.string.add_reminder_menu))
          .setLongLabel(getString(R.string.add_reminder_menu))
          .setIcon(Icon.createWithResource(this, R.drawable.add_reminder_shortcut))
          .setIntents(arrayOf(Intent(Intent.ACTION_MAIN).setClass(this, BottomNavActivity::class.java),
            Intent(Intent.ACTION_VIEW).setClass(this, CreateReminderActivity::class.java)))
          .build()

        val shortcut2 = ShortcutInfo.Builder(this, "id.note")
          .setShortLabel(getString(R.string.add_note))
          .setLongLabel(getString(R.string.add_note))
          .setIcon(Icon.createWithResource(this, R.drawable.add_note_shortcut))
          .setIntents(arrayOf(Intent(Intent.ACTION_MAIN).setClass(this, BottomNavActivity::class.java),
            Intent(Intent.ACTION_VIEW).setClass(this, CreateNoteActivity::class.java)))
          .build()

        if (gTasks.isLogged) {
          val shortcut3 = ShortcutInfo.Builder(this, "id.google.tasks")
            .setShortLabel(getString(R.string.add_google_task))
            .setLongLabel(getString(R.string.add_google_task))
            .setIcon(Icon.createWithResource(this, R.drawable.add_google_shortcut))
            .setIntents(arrayOf(Intent(Intent.ACTION_MAIN).setClass(this, BottomNavActivity::class.java),
              Intent(Intent.ACTION_VIEW).setClass(this, TaskActivity::class.java)))
            .build()
          shortcutManager.dynamicShortcuts = listOf(shortcut, shortcut2, shortcut3)
        } else {
          shortcutManager.dynamicShortcuts = listOf(shortcut, shortcut2)
        }
      }
    }
  }
}