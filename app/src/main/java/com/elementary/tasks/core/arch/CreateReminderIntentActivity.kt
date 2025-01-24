package com.elementary.tasks.core.arch

import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.core.deeplink.ReminderTextDeepLinkData
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DestinationScreen
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.activity.LightThemedActivity
import org.koin.android.ext.android.inject

class CreateReminderIntentActivity : LightThemedActivity() {

  private val navigator by inject<Navigator>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val action = intent.action
    val type = intent.type
    Logger.i(TAG, "Incoming intent with action: $action, type: $type")

    if (action == Intent.ACTION_SEND && "text/plain" == type) {
      val text = intent.getStringExtra(Intent.EXTRA_TEXT)
      if (text == null) {
        finish()
        return
      }
      val deepLinkData = ReminderTextDeepLinkData(text)
      navigator.navigate(
        ActivityDestination(
          screen = DestinationScreen.ReminderCreate,
          extras = Bundle().apply {
            putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
            putParcelable(deepLinkData.intentKey, deepLinkData)
          },
          flags = Intent.FLAG_ACTIVITY_NEW_TASK,
          isLoggedIn = true,
          action = Intent.ACTION_VIEW
        )
      )
    } else {
      Logger.i(TAG, "Unsupported action")
      finish()
    }
  }

  companion object {
    private const val TAG = "CreateReminderIntentActivity"
  }
}
