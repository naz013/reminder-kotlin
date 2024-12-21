package com.elementary.tasks.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.ThemedActivity
import com.elementary.tasks.core.os.ContextSwitcher
import com.github.naz013.feature.common.android.finishWith
import com.elementary.tasks.googletasks.task.GoogleTaskActivity
import com.elementary.tasks.home.BottomNavActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.ReminderBuilderLauncher
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : ThemedActivity() {

  private val viewModel by viewModel<SplashViewModel>()
  private val contextSwitcher by inject<ContextSwitcher>()
  private val reminderBuilderLauncher by inject<ReminderBuilderLauncher>()

  override fun onCreate(savedInstanceState: Bundle?) {
    // Handle the splash screen transition.
    val splashScreen = installSplashScreen()

    super.onCreate(savedInstanceState)

    contextSwitcher.switchContext(this)

    splashScreen.setKeepOnScreenCondition { true }

    lifecycle.addObserver(viewModel)
    viewModel.openHome.observe(this) {
      enableShortcuts()
      if (it) {
        finishWith(PinLoginActivity::class.java)
      } else {
        finishWith(BottomNavActivity::class.java)
      }
    }
  }

  private fun enableShortcuts() {
    val shortcutManager = getSystemService(ShortcutManager::class.java)
    if (shortcutManager != null) {
      val shortcut = ShortcutInfo.Builder(this, "id.reminder")
        .setShortLabel(getString(R.string.add_reminder_menu))
        .setLongLabel(getString(R.string.add_reminder_menu))
        .setIcon(Icon.createWithResource(this, R.drawable.add_reminder_shortcut))
        .setIntents(
          arrayOf(
            Intent(Intent.ACTION_MAIN).setClass(this, BottomNavActivity::class.java),
            Intent(Intent.ACTION_VIEW).setClass(this, reminderBuilderLauncher.getActivityClass())
          )
        )
        .build()

      val shortcut2 = ShortcutInfo.Builder(this, "id.note")
        .setShortLabel(getString(R.string.add_note))
        .setLongLabel(getString(R.string.add_note))
        .setIcon(Icon.createWithResource(this, R.drawable.add_note_shortcut))
        .setIntents(
          arrayOf(
            Intent(Intent.ACTION_MAIN).setClass(this, BottomNavActivity::class.java),
            Intent(Intent.ACTION_VIEW).setClass(this, CreateNoteActivity::class.java)
          )
        )
        .build()

      if (viewModel.isGoogleTasksEnabled) {
        val shortcut3 = ShortcutInfo.Builder(this, "id.google.tasks")
          .setShortLabel(getString(R.string.add_google_task))
          .setLongLabel(getString(R.string.add_google_task))
          .setIcon(Icon.createWithResource(this, R.drawable.add_google_shortcut))
          .setIntents(
            arrayOf(
              Intent(Intent.ACTION_MAIN)
                .setClass(this, BottomNavActivity::class.java),
              Intent(Intent.ACTION_VIEW)
                .setClass(this, GoogleTaskActivity::class.java)
            )
          )
          .build()
        shortcutManager.dynamicShortcuts = listOf(shortcut, shortcut2, shortcut3)
      } else {
        shortcutManager.dynamicShortcuts = listOf(shortcut, shortcut2)
      }
    }
  }
}
