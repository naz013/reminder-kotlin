package com.elementary.tasks.settings.other

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.databinding.ActivitySendFeedbackBinding

class SendFeedbackActivity : BindingActivity<ActivitySendFeedbackBinding>() {

  private val url = "https://docs.google.com/forms/d/1vOCBU-izJBQ8VAsA1zYtfHFxe9Q1-Qm9rp_pYG13B1s/viewform"

  override fun inflateBinding() = ActivitySendFeedbackBinding.inflate(layoutInflater)
  
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
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
    binding.toolbar.title = getString(R.string.feedback)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_feedback, menu)
    ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_refresh_24px, isDarkMode)
    ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_twotone_local_post_office_24px, isDarkMode)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_refresh -> {
        binding.webView.reload()
        return true
      }
      R.id.action_forward -> {
        if (binding.webView.canGoForward()) {
          binding.webView.goForward()
        }
        return true
      }
      R.id.action_back -> {
        if (binding.webView.canGoBack()) {
          binding.webView.goBack()
        }
        return true
      }
      R.id.action_email -> {
        sendEmail()
        return true
      }
      android.R.id.home -> {
        finish()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
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
