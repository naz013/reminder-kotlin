package com.elementary.tasks.navigation.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module

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

class FeedbackFragment : BaseWebViewFragment() {

    override val url: String
        get() = "https://docs.google.com/forms/d/1vOCBU-izJBQ8VAsA1zYtfHFxe9Q1-Qm9rp_pYG13B1s/viewform"

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_feedback, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_refresh -> {
                webView.reload()
                return true
            }
            R.id.action_forward -> {
                if (webView.canGoForward()) {
                    webView.goForward()
                }
                return true
            }
            R.id.action_back -> {
                if (webView.canGoBack()) {
                    webView.goBack()
                }
                return true
            }
            R.id.action_email -> {
                sendEmail()
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
