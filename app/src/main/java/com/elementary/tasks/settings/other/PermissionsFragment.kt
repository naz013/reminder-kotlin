package com.elementary.tasks.settings.other

import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeWebViewFragment

class PermissionsFragment : BaseComposeWebViewFragment() {
  override val url: String
    get() = "file:///android_asset/files/permissions.html"

  override fun getTitle(): String = getString(R.string.permissions)
}
