package com.elementary.tasks.core.arch

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import com.elementary.tasks.R
import com.github.naz013.files.AndroidDataConverter
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DataDestination
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.compose.ComposeActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class IntentActivity : ComposeActivity() {

  private val navigator by inject<Navigator>()
  private val androidDataConverter by inject<AndroidDataConverter>()
  private val importIntentResolver = ImportIntentResolver()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val data = intent.data ?: return
    val scheme = data.scheme

    Logger.i(TAG, "Incoming intent with data: $data, scheme: $scheme")

    if (!importIntentResolver.isSupportedScheme(scheme)) {
      Logger.i(TAG, "Unsupported scheme: $scheme")
      toast(getString(R.string.unsupported_file_format))
      finish()
      return
    }

    lifecycleScope.launch {
      val any = androidDataConverter.toData(data)
      Logger.i(TAG, "Parsed object: $any")

      when (val result = importIntentResolver.resolve(any)) {
        is ImportResult.Valid -> {
          navigator.navigate(DataDestination(result.data))
        }

        is ImportResult.Invalid -> {
          Logger.i(TAG, "Parsed object is NOT valid, reason: ${result.reason}")
          toast(getString(R.string.unsupported_file_format))
        }

        ImportResult.Unsupported -> {
          Logger.i(TAG, "Parsed object is not supported: ${any?.javaClass?.simpleName}")
          toast(getString(R.string.unsupported_file_format))
        }
      }

      finish()
    }
  }

  @Composable
  override fun ActivityContent() {

  }

  companion object {
    private const val TAG = "IntentActivity"
  }
}
