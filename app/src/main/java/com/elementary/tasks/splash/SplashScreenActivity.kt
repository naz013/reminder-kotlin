package com.elementary.tasks.splash

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.ThemedActivity
import com.elementary.tasks.core.utils.finishWith
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.home.BottomNavActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashScreenActivity : ThemedActivity() {

  private val viewModel by viewModel<SplashViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(viewModel)
    viewModel.openHome.observe(this) {
      enableShortcuts()
      if (it) finishWith(PinLoginActivity::class.java)
      else finishWith(BottomNavActivity::class.java)
    }
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

        if (viewModel.isGoogleTasksEnabled) {
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