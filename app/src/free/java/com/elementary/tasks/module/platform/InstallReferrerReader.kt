package com.elementary.tasks.module.platform

import android.content.Context
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender

/**
 * No-op on the free flavor: install-referrer attribution only matters for PRO installs, which
 * only happen on the pro flavor - see the pro-flavor implementation of this class.
 */
class InstallReferrerReader(
  private val context: Context,
  private val prefs: Prefs,
  private val analyticsEventSender: AnalyticsEventSender,
) {
  fun readOnce() {}
}
