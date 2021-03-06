package com.elementary.tasks.navigation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.elementary.tasks.databinding.FragmentSettingsWebViewBinding
import com.elementary.tasks.settings.BaseSettingsFragment

abstract class BaseWebViewFragment : BaseSettingsFragment<FragmentSettingsWebViewBinding>() {

  protected val webView: WebView
    get() = binding.webView

  protected abstract val url: String

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsWebViewBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setExtraParams(binding.webView)
    binding.webView.loadUrl(url)
  }

  protected open fun setExtraParams(webView: WebView) {

  }
}
