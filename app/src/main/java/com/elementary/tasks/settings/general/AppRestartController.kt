package com.elementary.tasks.settings.general

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import com.elementary.tasks.splash.SplashScreenActivity
import com.github.naz013.ui.common.activity.finishWith
import com.google.android.material.color.DynamicColors

interface AppRestartController {
  fun recreate()
  fun applyDynamicColorsAndRecreate(useDynamicColors: Boolean)
  fun restartApp()
}

private class AppRestartControllerImpl(
  private val activity: Activity,
) : AppRestartController {

  override fun recreate() {
    activity.recreate()
  }

  override fun applyDynamicColorsAndRecreate(useDynamicColors: Boolean) {
    if (useDynamicColors) DynamicColors.applyToActivityIfAvailable(activity)
    activity.recreate()
  }

  override fun restartApp() {
    activity.finishWith(SplashScreenActivity::class.java)
  }
}

@Composable
fun rememberAppRestartController(): AppRestartController {
  val activity = LocalActivity.current as Activity
  return AppRestartControllerImpl(activity)
}
