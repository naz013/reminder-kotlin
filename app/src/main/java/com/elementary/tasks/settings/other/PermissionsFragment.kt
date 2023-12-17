package com.elementary.tasks.settings.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsWebViewBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment

class PermissionsFragment : BaseSettingsFragment<FragmentSettingsWebViewBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsWebViewBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val url = "file:///android_asset/files/permissions.html"
    binding.webView.loadUrl(url)
  }

  override fun getTitle(): String = getString(R.string.permissions)
}
