package com.github.naz013.feature.reminder.settings.help

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
 * "How Notification Customization Works" help guide: explains the Settings -> Group -> Reminder
 * notification-override hierarchy and calls out that some options depend on the device/Android
 * version. Displayed in a WebView within a Material 3 Scaffold, same pattern as
 * [com.github.naz013.feature.reminder.build.help.ReminderHelpScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCustomizationHelpScreen(onBackClick: () -> Unit) {
  var isScrolled by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(text = stringResource(R.string.notification_customization))
        },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackClick,
          )
        },
        colors = TopAppbarColor,
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior().apply {
          state.contentOffset = if (isScrolled) -1f else 0f
        },
      )
    },
  ) { paddingValues ->
    AndroidView(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      factory = { context ->
        WebView(context).apply {
          layoutParams =
            ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT,
            )

          setOnScrollChangeListener { _, _, scrollY, _, _ ->
            isScrolled = scrollY > 0
          }

          loadUrl("file:///android_asset/files/notification_customization.html")
        }
      },
      update = { webView ->
        webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
          isScrolled = scrollY > 0
        }
      },
    )
  }
}
