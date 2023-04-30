package com.elementary.tasks.settings.other

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.databinding.ActivitySendFeedbackBinding
import com.elementary.tasks.navigation.fragments.FeedbackFragment

class SendFeedbackActivity : BindingActivity<ActivitySendFeedbackBinding>() {

  private val url = FeedbackFragment.FORM_URL

  override fun inflateBinding() = ActivitySendFeedbackBinding.inflate(layoutInflater)

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()

    binding.webView.settings.javaScriptEnabled = true
    binding.webView.webViewClient = object : WebViewClient() {
      override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
      ) {
      }

      override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        return if (url != null && url.contains(FeedbackFragment.GITHUB_URL)) {
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
    binding.toolbar.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.action_refresh -> {
          binding.webView.reload()
          true
        }

        R.id.action_forward -> {
          if (binding.webView.canGoForward()) {
            binding.webView.goForward()
          }
          true
        }

        R.id.action_back -> {
          if (binding.webView.canGoBack()) {
            binding.webView.goBack()
          }
          true
        }

        R.id.action_email -> {
          sendEmail()
          true
        }

        else -> false
      }
    }
    binding.toolbar.title = getString(R.string.feedback)
  }

  private fun sendEmail() {
    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.type = "plain/text"
    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("feedback.cray@gmail.com"))
    if (Module.isPro) {
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reminder PRO")
    } else {
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reminder")
    }
    startActivity(Intent.createChooser(emailIntent, "Send mail..."))
  }
}
