package com.github.naz013.feature.settings.general

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable

interface AppRestartController {
  fun recreate()
  fun restartApp()
}

private class AppRestartControllerImpl(
  private val activity: Activity,
  private val restartActivityClass: Class<out Activity>,
) : AppRestartController {

  override fun recreate() {
    activity.recreate()
  }

  /**
   * The restart target is typically `singleInstance` and owns the app's splash/init pipeline, so
   * a plain start+finish would just redeliver the intent to the existing instance via
   * `onNewIntent` without recreating it - CLEAR_TASK forces a genuinely fresh instance (fresh
   * ViewModelStore, so its init ViewModel reruns its init work).
   */
  override fun restartApp() {
    val intent =
      Intent(activity, restartActivityClass)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    activity.startActivity(intent)
  }
}

@Composable
fun rememberAppRestartController(restartActivityClass: Class<out Activity>): AppRestartController {
  val activity = LocalActivity.current as Activity
  return AppRestartControllerImpl(activity, restartActivityClass)
}
