package com.elementary.tasks.settings.general

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import com.google.android.material.color.DynamicColors

interface AppRestartController {
  fun recreate()
  fun applyDynamicColorsAndRecreate(useDynamicColors: Boolean)
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
}

@Composable
fun rememberAppRestartController(): AppRestartController {
  val activity = LocalActivity.current as Activity
  return AppRestartControllerImpl(activity)
}
