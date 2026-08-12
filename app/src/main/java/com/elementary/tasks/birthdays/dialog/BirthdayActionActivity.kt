package com.elementary.tasks.birthdays.dialog

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elementary.tasks.ads.AdBanner
import com.elementary.tasks.ads.NormalAdBanner
import com.elementary.tasks.telephony.rememberPhoneCaller
import com.elementary.tasks.telephony.rememberSmsSender
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.birthday.dialog.BirthdayActionScreen
import com.github.naz013.ui.common.compose.ComposeActivity
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.context.startActivity

class BirthdayActionActivity : ComposeActivity() {

  @Composable
  override fun ActivityContent() {
    val phoneCaller = rememberPhoneCaller()
    val smsSender = rememberSmsSender()
    BirthdayActionScreen(
      id = getId(),
      onFinish = { finish() },
      onCallClick = { number -> phoneCaller.call(number) },
      onSmsClick = { number -> smsSender.send(number, null) },
      adsContent = { NormalAdBanner(modifier = Modifier.fillMaxWidth(), adBanner = AdBanner.ActionScreen) }
    )
  }

  private fun getId() = intent.getStringExtra(IntentKeys.INTENT_ID) ?: ""

  companion object {
    private const val TAG = "BirthdayActionActivity"
    private const val ARG_TEST = "arg_test"

    fun mockTest(
      context: Context,
      id: String,
    ) {
      context.startActivity(BirthdayActionActivity::class.java) {
        putExtra(ARG_TEST, true)
        putExtra(IntentKeys.INTENT_ID, id)
      }
    }

    fun getLaunchIntent(
      context: Context,
      id: String,
    ): Intent =
      context.buildIntent(BirthdayActionActivity::class.java) {
        putExtra(IntentKeys.INTENT_ID, id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
      }
  }
}
