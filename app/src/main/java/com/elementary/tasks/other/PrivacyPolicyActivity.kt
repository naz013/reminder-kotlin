package com.elementary.tasks.other

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.databinding.ActivityPrivacyPolicyBinding
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.applyTopInsets
import org.koin.android.ext.android.inject

class PrivacyPolicyActivity : BindingActivity<ActivityPrivacyPolicyBinding>() {

  private val prefs by inject<Prefs>()
  private val url by lazy { prefs.privacyUrl }

  override fun inflateBinding() = ActivityPrivacyPolicyBinding.inflate(layoutInflater)

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    initActionBar()

    binding.webView.applyBottomInsets()
    binding.webView.settings.javaScriptEnabled = true
    binding.webView.webViewClient = object : WebViewClient() {
      @Deprecated("Deprecated in Java")
      override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
      ) {
      }

      @Deprecated("Deprecated in Java")
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
    binding.appBar.applyTopInsets()
    binding.toolbar.setNavigationOnClickListener { finish() }
    binding.toolbar.title = getString(R.string.privacy_policy)
  }
}
