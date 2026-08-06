package com.github.naz013.ui.common.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable

abstract class ComposeActivity : ComponentActivity() {

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
