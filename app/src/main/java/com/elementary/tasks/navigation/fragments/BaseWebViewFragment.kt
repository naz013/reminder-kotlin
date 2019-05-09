package com.elementary.tasks.navigation.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsWebViewBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

abstract class BaseWebViewFragment : BaseSettingsFragment<FragmentSettingsWebViewBinding>() {

    protected val webView: WebView
        get() = binding.webView

    protected abstract val url: String

    override fun layoutRes(): Int = R.layout.fragment_settings_web_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setExtraParams(binding.webView)
        binding.webView.loadUrl(url)
    }

    protected open fun setExtraParams(webView: WebView) {

    }
}
