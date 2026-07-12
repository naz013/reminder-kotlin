package com.elementary.tasks.core.os.datapicker.compose

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.elementary.tasks.core.apps.SelectApplicationActivity
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.context.intentForClass

/**
 * Compose replacement for [com.elementary.tasks.core.os.datapicker.ApplicationPicker]. Needs no
 * Fragment/Activity reference - [rememberLauncherForActivityResult] resolves the
 * `ActivityResultRegistry` from the current composition, matching
 * [com.elementary.tasks.core.os.datapicker.compose.rememberContactPicker].
 *
 * @return a trigger function taking a per-call result callback, matching
 * [com.elementary.tasks.reminder.build.valuedialog.ValueEditorSheet]'s
 * `onPickApplication: (onResult: (String) -> Unit) -> Unit` shape.
 */
@Composable
fun rememberApplicationPicker(): (onResult: (String) -> Unit) -> Unit {
  val context = LocalContext.current
  val pendingCallback = remember { PendingCallback<String>() }
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val appPackage = result.data?.getStringExtra(IntentKeys.SELECTED_APPLICATION) ?: ""
        pendingCallback.value?.invoke(appPackage)
      }
      pendingCallback.value = null
    }
  return remember(launcher, context) {
    { onResult ->
      pendingCallback.value = onResult
      launcher.launch(context.intentForClass(SelectApplicationActivity::class.java))
    }
  }
}

internal class PendingCallback<T> {
  var value: ((T) -> Unit)? = null
}
