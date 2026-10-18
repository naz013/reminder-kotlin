package com.elementary.tasks.module.platform

import com.elementary.tasks.BuildConfig
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.system.BuildInfo

class BuildInfoImpl(
  private val prefs: Prefs,
) : BuildInfo {
  private val proTrial = ProTrial()

  override val isPro: Boolean
    get() = BuildConfig.IS_PRO || !proTrial.isExpired(prefs.proTrialExpiryMillis)
  override val isDebug: Boolean = BuildConfig.DEBUG
  override val applicationId: String = BuildConfig.APPLICATION_ID
  override val buildDate: String = BuildConfig.BUILD_DATE
  override val googleSignInServerClientId: String = BuildConfig.GOOGLE_SIGN_IN_SERVER_CLIENT_ID
}
