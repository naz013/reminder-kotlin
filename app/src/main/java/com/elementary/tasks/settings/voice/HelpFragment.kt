package com.elementary.tasks.settings.voice

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.dialogs.VoiceHelpActivity
import com.elementary.tasks.core.dialogs.VoiceHelpViewModel
import com.elementary.tasks.navigation.fragments.BaseWebViewFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class HelpFragment : BaseWebViewFragment() {

  private val viewModel by viewModel<VoiceHelpViewModel>()
  override val url: String
    get() {
      return VoiceHelpActivity.getHelpUrl(
        language.getVoiceLocale(prefs.voiceLocale),
        viewModel.urls.value
      ) ?: ""
    }

  @SuppressLint("SetJavaScriptEnabled")
  override fun setExtraParams(webView: WebView) {
    super.setExtraParams(webView)
    webView.settings.javaScriptEnabled = true
    webView.webViewClient = object : WebViewClient() {
      @Deprecated("Deprecated in Java")
      override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
      ) {
      }

      @Deprecated("Deprecated in Java")
      override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
      }
    }
    webView.webChromeClient = WebChromeClient()
    lifecycle.addObserver(viewModel)
    viewModel.urls.observe(this) { reload() }
  }

  override fun getTitle(): String = getString(R.string.help)
}
