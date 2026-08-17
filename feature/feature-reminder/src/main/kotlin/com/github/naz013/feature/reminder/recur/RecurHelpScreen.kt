package com.github.naz013.feature.reminder.recur

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.TopAppbarColor

private const val URL = "file:///android_asset/files/doc_rfc_5545.html"

/** RFC 5545 recurrence-rule reference doc, opened from the iCal group value editors' help button. */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecurHelpScreen(onBackClick: () -> Unit) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(R.string.recur_rfc_5545_doc)) },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              painter = AppIcons.Builder.ArrowLeft,
              contentDescription = stringResource(com.github.naz013.ui.common.R.string.cd_back),
            )
          }
        },
        colors = TopAppbarColor,
      )
    },
  ) { paddingValues ->
    AndroidView(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      factory = { context ->
        WebView(context).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
          )
          settings.javaScriptEnabled = true
          webViewClient = object : WebViewClient() {}
          webChromeClient = WebChromeClient()
          loadUrl(URL)
        }
      },
    )
  }
}
