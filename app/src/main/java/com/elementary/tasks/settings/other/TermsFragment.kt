package com.elementary.tasks.settings.other

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.navigation.fragments.BaseWebViewFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class TermsFragment : BaseWebViewFragment() {
  private val viewModel by viewModel<TermsViewModel>()

  override val url: String
    get() = viewModel.url

  @SuppressLint("SetJavaScriptEnabled")
  override fun setExtraParams(webView: WebView) {
    super.setExtraParams(webView)
    webView.settings.javaScriptEnabled = true
    webView.webViewClient =
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
    webView.webChromeClient = WebChromeClient()
  }

  override fun getTitle(): String = getString(R.string.terms_and_conditions)
}
