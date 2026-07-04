package com.elementary.tasks.navigation.toolbarfragment

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Compose counterpart of the legacy WebView-backed settings screens. Compose has no native
 * WebView replacement, so the view is hosted via [AndroidView].
 */
abstract class BaseComposeWebViewFragment : BaseComposeToolbarFragment() {
  protected abstract val url: String

  @Composable
  final override fun Content() {
    val documentUrl = url
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { context -> WebView(context).apply { loadDocument(documentUrl) } },
    )
  }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.loadDocument(url: String) {
  settings.javaScriptEnabled = true
  webViewClient =
    object : WebViewClient() {
      @Deprecated("Deprecated in Java")
      override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String,
      ) {
      }

      @Deprecated("Deprecated in Java")
      override fun shouldOverrideUrlLoading(
        view: WebView,
        url: String,
      ): Boolean {
        view.loadUrl(url)
        return true
      }
    }
  webChromeClient = WebChromeClient()
  loadUrl(url)
}
