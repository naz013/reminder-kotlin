package com.elementary.tasks.core.apps

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.compose.ComposeActivity

@Deprecated("After S")
class SelectApplicationActivity : ComposeActivity() {

  @Composable
  override fun ActivityContent() {
    SelectApplicationScreen(
      onBackClick = { cancelAndFinish() },
      onAppSelected = { packageName -> selectAppAndFinish(packageName) },
    )
  }

  private fun selectAppAndFinish(packageName: String) {
    val intent = Intent().putExtra(IntentKeys.SELECTED_APPLICATION, packageName)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  private fun cancelAndFinish() {
    setResult(Activity.RESULT_CANCELED, Intent())
    finish()
  }
}
