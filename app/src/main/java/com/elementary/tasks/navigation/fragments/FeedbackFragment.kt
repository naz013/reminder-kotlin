package com.elementary.tasks.navigation.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ViewUtils

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_feedback, menu)

        ViewUtils.tintMenuIcon(context!!, menu, 0, R.drawable.ic_twotone_refresh_24px, isDark)
        ViewUtils.tintMenuIcon(context!!, menu, 1, R.drawable.ic_twotone_local_post_office_24px, isDark)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
