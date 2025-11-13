package com.github.naz013.ui.common.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable

abstract class ComposeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    setContent {
      AppTheme {
        ActivityContent()
      }
    }
  }

  @Composable
  abstract fun ActivityContent()
}
