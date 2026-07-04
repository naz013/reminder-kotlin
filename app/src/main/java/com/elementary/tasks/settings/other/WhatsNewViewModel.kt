package com.elementary.tasks.settings.other

import androidx.lifecycle.ViewModel
import com.elementary.tasks.BuildConfig
import com.github.naz013.common.PackageManagerWrapper
import org.apache.commons.lang3.StringUtils

class WhatsNewViewModel(
  packageManagerWrapper: PackageManagerWrapper,
) : ViewModel() {
  val versionName: String = packageManagerWrapper.getVersionName()
  val buildDate: String = StringUtils.capitalize(BuildConfig.BUILD_DATE)
}
