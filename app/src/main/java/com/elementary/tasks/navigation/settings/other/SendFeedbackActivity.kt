package com.elementary.tasks.navigation.settings.other

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
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_send_feedback.*

/**
 * Copyright 2019 Nazar Suhovich
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
class SendFeedbackActivity : ThemedActivity() {

    private val url = "https://docs.google.com/forms/d/1vOCBU-izJBQ8VAsA1zYtfHFxe9Q1-Qm9rp_pYG13B1s/viewform"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_feedback)
        initActionBar()

        web_view.settings.javaScriptEnabled = true
        web_view.webViewClient = object : WebViewClient() {
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
        web_view.webChromeClient = WebChromeClient()
        web_view.loadUrl(url)
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ViewUtils.backIcon(this, isDark)
        toolbar.title = getString(R.string.feedback)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_feedback, menu)
        ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_refresh_24px, isDark)
        ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_twotone_local_post_office_24px, isDark)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_refresh -> {
                web_view.reload()
                return true
            }
            R.id.action_forward -> {
                if (web_view.canGoForward()) {
                    web_view.goForward()
                }
                return true
            }
            R.id.action_back -> {
                if (web_view.canGoBack()) {
                    web_view.goBack()
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
