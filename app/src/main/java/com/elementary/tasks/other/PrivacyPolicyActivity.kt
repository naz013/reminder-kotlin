package com.elementary.tasks.other

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : BindingActivity<ActivityPrivacyPolicyBinding>() {

  private val url = prefs.privacyUrl

  override fun inflateBinding() = ActivityPrivacyPolicyBinding.inflate(layoutInflater)

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()

    binding.webView.settings.javaScriptEnabled = true
    binding.webView.webViewClient = object : WebViewClient() {
      override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {}

      override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        return if (url != null && url.contains("https://github.com/naz013/Reminder/issues")) {
          startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
          true
        } else {
          false
        }
      }
    }
    binding.webView.webChromeClient = WebChromeClient()
    binding.webView.loadUrl(url)
  }

  private fun initActionBar() {
    binding.toolbar.setNavigationOnClickListener { finish() }
    binding.toolbar.title = getString(R.string.privacy_policy)
  }
}
