package com.elementary.tasks.navigation.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ui.ViewUtils

class FeedbackFragment : BaseWebViewFragment() {

  override val url: String
    get() = "https://docs.google.com/forms/d/1vOCBU-izJBQ8VAsA1zYtfHFxe9Q1-Qm9rp_pYG13B1s/viewform"

  @SuppressLint("SetJavaScriptEnabled")
  override fun setExtraParams(webView: WebView) {
    super.setExtraParams(webView)
    webView.settings.javaScriptEnabled = true
    webView.webViewClient = object : WebViewClient() {
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
    webView.webChromeClient = WebChromeClient()
  }

  override fun getTitle(): String = getString(R.string.feedback)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    addMenu(R.menu.menu_feedback, { menuItem ->
      return@addMenu when (menuItem.itemId) {
        R.id.action_refresh -> {
          webView.reload()
          true
        }
        R.id.action_forward -> {
          if (webView.canGoForward()) {
            webView.goForward()
          }
          true
        }
        R.id.action_back -> {
          if (webView.canGoBack()) {
            webView.goBack()
          }
          true
        }
        R.id.action_email -> {
          sendEmail()
          true
        }
        else -> false
      }
    }) {
      ViewUtils.tintMenuIcon(requireContext(), it, 0, R.drawable.ic_twotone_refresh_24px, isDark)
      ViewUtils.tintMenuIcon(requireContext(), it, 1, R.drawable.ic_twotone_local_post_office_24px, isDark)
    }
    activity?.invalidateOptionsMenu()
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
