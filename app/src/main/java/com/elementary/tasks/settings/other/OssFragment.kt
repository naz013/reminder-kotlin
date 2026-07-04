package com.elementary.tasks.settings.other

import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeWebViewFragment

class OssFragment : BaseComposeWebViewFragment() {
  override val url: String
    get() = "file:///android_asset/files/oss.html"

  override fun getTitle(): String = getString(R.string.open_source_licenses)
}
