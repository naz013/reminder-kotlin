package com.elementary.tasks.settings.general

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import com.elementary.tasks.navigation.BottomNavActivity

interface AppRestartController {
  fun recreate()
  fun restartApp()
}

private class AppRestartControllerImpl(
  private val activity: Activity,
) : AppRestartController {

  override fun recreate() {
    activity.recreate()
  }

  /**
   * [BottomNavActivity] is `singleInstance` and now owns the splash/init pipeline, so a plain
   * start+finish would just redeliver the intent to the existing instance via `onNewIntent`
   * without recreating it - CLEAR_TASK forces a genuinely fresh instance (fresh ViewModelStore,
   * so [com.elementary.tasks.navigation.BottomNavInitViewModel] reruns its init work).
   */
  override fun restartApp() {
    val intent =
      Intent(activity, BottomNavActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    activity.startActivity(intent)
  }
}

@Composable
fun rememberAppRestartController(): AppRestartController {
  val activity = LocalActivity.current as Activity
  return AppRestartControllerImpl(activity)
}
