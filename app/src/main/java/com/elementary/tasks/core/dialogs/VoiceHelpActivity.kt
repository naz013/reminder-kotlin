package com.elementary.tasks.core.dialogs

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_voice_help.*
import java.util.*

/**
 * Copyright 2017 Nazar Suhovich
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
class VoiceHelpActivity : ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_help)
        initActionBar()
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {}

            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl(getHelpUrl(language.getVoiceLocale(prefs.voiceLocale)))
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
        toolbar.title = getString(R.string.help)
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
            val localeCheck = locale.toString().toLowerCase()
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
