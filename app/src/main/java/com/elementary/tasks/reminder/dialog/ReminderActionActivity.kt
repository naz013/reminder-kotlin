package com.elementary.tasks.reminder.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DestinationScreen
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.compose.ComposeActivity
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.context.startActivity
import org.koin.android.ext.android.inject

class ReminderActionActivity : ComposeActivity() {

  private val navigator by inject<Navigator>()

  @Composable
  override fun ActivityContent() {
    ReminderActionScreen(
      id = getId(),
      onFinish = { finish() },
      onEdit = {
        navigator.navigate(
          ActivityDestination(
            screen = DestinationScreen.ReminderCreate,
            extras =
              Bundle().apply {
                putString(IntentKeys.INTENT_ID, it)
              },
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true,
            action = Intent.ACTION_VIEW,
          ),
        )
        finish()
      },
      adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), adBanner = AdBanner.ActionScreen) }
    )
  }

  private fun getId() = intent.getStringExtra(IntentKeys.INTENT_ID) ?: ""

  companion object {
    private const val TAG = "ReminderActionActivity"
    private const val ARG_TEST = "arg_test"

    fun mockTest(
      context: Context,
      id: String,
    ) {
      context.startActivity(ReminderActionActivity::class.java) {
        putExtra(ARG_TEST, true)
        putExtra(IntentKeys.INTENT_ID, id)
      }
    }

    fun getLaunchIntent(
      context: Context,
      id: String,
    ): Intent =
      context.buildIntent(ReminderActionActivity::class.java) {
        putExtra(IntentKeys.INTENT_ID, id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      }
  }
}
