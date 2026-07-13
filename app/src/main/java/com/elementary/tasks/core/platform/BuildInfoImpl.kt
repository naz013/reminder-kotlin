package com.elementary.tasks.core.platform

import com.elementary.tasks.BuildConfig
import com.github.naz013.common.system.BuildInfo

class BuildInfoImpl : BuildInfo {
  override val isPro: Boolean = BuildConfig.IS_PRO
  override val isDebug: Boolean = BuildConfig.DEBUG
}
