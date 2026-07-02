package com.github.naz013.ui.common.compose

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.github.naz013.ui.common.activity.LightThemedActivity

abstract class LightThemedComposeActivity : LightThemedActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    composeView {
      ActivityContent()
    }
  }

  @Composable
  abstract fun ActivityContent()
}
