package com.github.naz013.ui.common.compose

import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable

fun AppCompatActivity.composeView(content: @Composable () -> Unit) {
  setContent { AppTheme { content() } }
}
