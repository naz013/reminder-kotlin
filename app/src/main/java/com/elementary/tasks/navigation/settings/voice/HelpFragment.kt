package com.elementary.tasks.navigation.settings.voice

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.dialogs.VoiceHelpDialog
import com.elementary.tasks.navigation.fragments.BaseWebViewFragment

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class HelpFragment : BaseWebViewFragment() {

    override val url: String
        get() {
            return VoiceHelpDialog.getHelpUrl(language.getVoiceLocale(prefs.voiceLocale))
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun setExtraParams(webView: WebView) {
        super.setExtraParams(webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {}

            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        webView.webChromeClient = WebChromeClient()
    }

    override fun getTitle(): String = getString(R.string.help)
}
