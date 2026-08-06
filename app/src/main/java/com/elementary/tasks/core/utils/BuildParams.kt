package com.elementary.tasks.core.utils

import com.elementary.tasks.BuildConfig

@Deprecated("Use BuildInfo instead")
object BuildParams {
  const val isPro: Boolean = BuildConfig.IS_PRO
}
