package com.elementary.tasks.core.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.databinding.ActivityVoiceHelpBinding
import com.github.naz013.ui.common.activity.BindingActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class VoiceHelpActivity : BindingActivity<ActivityVoiceHelpBinding>() {

  private val prefs by inject<Prefs>()
  private val viewModel by viewModel<VoiceHelpViewModel>()

  override fun inflateBinding() = ActivityVoiceHelpBinding.inflate(layoutInflater)

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()
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
      override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
      }
    }
    binding.webView.webChromeClient = WebChromeClient()
    lifecycle.addObserver(viewModel)
    viewModel.urls.observe(this) { urls ->
      getHelpUrl(
        language.getVoiceLocale(prefs.voiceLocale),
        urls
      )?.also { binding.webView.loadUrl(it) }
    }
  }

  private fun initActionBar() {
    binding.toolbar.setNavigationOnClickListener { finish() }
  }

  companion object {
    fun getHelpUrl(
      locale: Locale = Locale.getDefault(),
      urls: VoiceHelpViewModel.Urls? = null
    ): String? {
      val localeCheck = locale.toString().lowercase()
      val urlsData = urls?.urls?.firstOrNull { it.lang == localeCheck } ?: return null
      return urlsData.url
    }
  }
}
