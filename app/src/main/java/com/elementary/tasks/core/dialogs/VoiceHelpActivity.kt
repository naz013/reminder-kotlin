package com.elementary.tasks.core.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.databinding.ActivityVoiceHelpBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class VoiceHelpActivity : BindingActivity<ActivityVoiceHelpBinding>() {

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
