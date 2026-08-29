package com.elementary.tasks.module.platform

import com.elementary.tasks.BuildConfig
import com.github.naz013.common.system.BuildInfo

class BuildInfoImpl : BuildInfo {
  override val isPro: Boolean = BuildConfig.IS_PRO
  override val isDebug: Boolean = BuildConfig.DEBUG
  override val applicationId: String = BuildConfig.APPLICATION_ID
  override val buildDate: String = BuildConfig.BUILD_DATE
  override val googleSignInServerClientId: String = BuildConfig.GOOGLE_SIGN_IN_SERVER_CLIENT_ID
}
