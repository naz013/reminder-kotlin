package com.github.naz013.ui.common.compose

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.github.naz013.ui.common.theme.ThemeProvider
import org.koin.android.ext.android.inject

fun AppCompatActivity.composeView(content: @Composable () -> Unit) {
  val themeProvider: ThemeProvider by inject()
  setContent { AppTheme(darkTheme = themeProvider.isDark) { content() } }
}

fun ComponentActivity.composeView(content: @Composable () -> Unit) {
  val themeProvider: ThemeProvider by inject()
  setContent { AppTheme(darkTheme = themeProvider.isDark) { content() } }
}
