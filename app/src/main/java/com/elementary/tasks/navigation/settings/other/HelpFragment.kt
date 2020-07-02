package com.elementary.tasks.navigation.settings.other

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.navigation.fragments.BaseWebViewFragment
import java.util.Locale

class HelpFragment : BaseWebViewFragment() {

    override val url: String
        get() {
            val localeCheck = Locale.getDefault().toString().toLowerCase()
            return when {
                localeCheck.startsWith("uk") -> Constants.WEB_URL + "reminder-help-ukrainian"
                localeCheck.startsWith("ru") -> Constants.WEB_URL + "reminder-help-russian"
                else -> Constants.WEB_URL + "reminder-help-english"
            }
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun setExtraParams(webView: WebView) {
        super.setExtraParams(webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {}

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        webView.webChromeClient = WebChromeClient()
    }

    override fun getTitle(): String = getString(R.string.help)
}
