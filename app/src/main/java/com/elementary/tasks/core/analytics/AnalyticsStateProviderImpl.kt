package com.elementary.tasks.core.analytics

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsStateProvider

class AnalyticsStateProviderImpl(
  private val prefs: Prefs
) : AnalyticsStateProvider {
  override val analyticsEnabled: Boolean
    get() = prefs.analyticsEnabled
}
