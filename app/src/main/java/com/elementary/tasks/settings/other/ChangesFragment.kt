package com.elementary.tasks.settings.other

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsWebViewBinding
import com.elementary.tasks.settings.BaseSettingsFragment

class ChangesFragment : BaseSettingsFragment<FragmentSettingsWebViewBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsWebViewBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val url = "file:///android_asset/files/change_log.html"
    binding.webView.loadUrl(url)
    binding.webView.webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if (url.contains("craysoftware.wordpress.com")) {
          startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
          return true
        }
        return false
      }
    }
  }

  override fun getTitle(): String = getString(R.string.changes)
}
