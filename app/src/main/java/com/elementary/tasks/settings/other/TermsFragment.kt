package com.elementary.tasks.settings.other

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.navigation.fragments.BaseWebViewFragment

class TermsFragment : BaseWebViewFragment() {

  override val url: String
    get() {
      return prefs.termsUrl
    }

  @SuppressLint("SetJavaScriptEnabled")
  override fun setExtraParams(webView: WebView) {
    super.setExtraParams(webView)
    webView.settings.javaScriptEnabled = true
    webView.webViewClient = object : WebViewClient() {
      override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {}

      override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
      }
    }
    webView.webChromeClient = WebChromeClient()
  }

  override fun getTitle(): String = getString(R.string.terms_and_conditions)
}
