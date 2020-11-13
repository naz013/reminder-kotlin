package com.elementary.tasks.navigation.settings.other

import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsWebViewBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

class OssFragment : BaseSettingsFragment<FragmentSettingsWebViewBinding>() {

  override fun layoutRes(): Int = R.layout.fragment_settings_web_view

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val url = "file:///android_asset/files/oss.html"
    binding.webView.loadUrl(url)
  }

  override fun getTitle(): String = getString(R.string.open_source_licenses)
}
