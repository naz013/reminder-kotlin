package com.github.naz013.feature.reminder.build.help

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

/**
 * "How to Create a Reminder" help guide: a comprehensive HTML-based guide covering the reminder
 * builder's workflow, item categories, use cases, presets, validation, permissions, and
 * troubleshooting - displayed in a WebView within a Material 3 Scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReminderHelpScreen(onBackClick: () -> Unit) {
  // Track whether the WebView has been scrolled
  var isScrolled by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(text = stringResource(R.string.how_to_create_a_reminder))
        },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackClick,
          )
        },
        colors = TopAppbarColor,
        scrollBehavior =
          TopAppBarDefaults.enterAlwaysScrollBehavior().apply {
            state.contentOffset = if (isScrolled) -1f else 0f
          },
      )
    },
  ) { paddingValues ->
    // WebView to display the HTML help document
    AndroidView(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(paddingValues),
      factory = { context ->
        WebView(context).apply {
          layoutParams =
            ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT,
            )

          // Set up scroll listener to detect when content is scrolled
          setOnScrollChangeListener { _, _, scrollY, _, _ ->
            isScrolled = scrollY > 0
          }

          // Load the HTML file from assets, alongside the shared style.css
          loadUrl("file:///android_asset/files/how_to_create_a_reminder.html")
        }
      },
      update = { webView ->
        // Update scroll listener to track state changes
        webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
          isScrolled = scrollY > 0
        }
      },
    )
  }
}
