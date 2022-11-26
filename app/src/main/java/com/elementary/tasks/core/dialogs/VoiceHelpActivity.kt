package com.elementary.tasks.core.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityVoiceHelpBinding
import java.util.*

class VoiceHelpActivity : BindingActivity<ActivityVoiceHelpBinding>() {

  override fun inflateBinding() = ActivityVoiceHelpBinding.inflate(layoutInflater)

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()
    binding.webView.settings.javaScriptEnabled = true
    binding.webView.webViewClient = object : WebViewClient() {
      override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {}

      override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
      }
    }
    binding.webView.webChromeClient = WebChromeClient()
    binding.webView.loadUrl(getHelpUrl(language.getVoiceLocale(prefs.voiceLocale)))
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
    binding.toolbar.title = getString(R.string.help)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
      else -> super.onOptionsItemSelected(item)
    }
  }

  companion object {
    fun getHelpUrl(locale: Locale = Locale.getDefault()): String {
      val localeCheck = locale.toString().lowercase()
      return when {
        localeCheck.startsWith("uk") -> Constants.WEB_URL + "reminder-voice-ukrainian"
        localeCheck.startsWith("ru") -> Constants.WEB_URL + "reminder-voice-russian"
        localeCheck.startsWith("de") -> Constants.WEB_URL + "reminder-voice-german"
        localeCheck.startsWith("es") -> Constants.WEB_URL + "reminder-voice-spanish"
        localeCheck.startsWith("pt") -> Constants.WEB_URL + "reminder-voice-portuguese"
        else -> Constants.WEB_URL + "reminder-voice-english"
      }
    }
  }
}
