package com.elementary.tasks.reminder.create.fragments.recur

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.databinding.ActivityWebViewBinding

class RecurHelpActivity : BindingActivity<ActivityWebViewBinding>() {

  override fun inflateBinding() = ActivityWebViewBinding.inflate(layoutInflater)

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()

    binding.webView.settings.javaScriptEnabled = true
    binding.webView.webViewClient = object : WebViewClient() {}
    binding.webView.webChromeClient = WebChromeClient()
    binding.webView.loadUrl(URL)
  }

  private fun initActionBar() {
    binding.toolbar.setNavigationOnClickListener { finish() }
    binding.toolbar.title = getString(R.string.recur_rfc_5545_doc)
  }

  companion object {
    private const val URL = "file:///android_asset/files/doc_rfc_5545.html"
  }
}
