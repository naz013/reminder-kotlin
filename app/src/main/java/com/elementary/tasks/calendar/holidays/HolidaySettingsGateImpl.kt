package com.elementary.tasks.calendar.holidays

import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.holidaysapi.HolidaySettingsGate

/**
 * Deliberately does not check [com.github.naz013.common.system.BuildInfo.isPro] - that's a
 * separate, purely presentational concern for the settings UI (lock/upsell state). This answers
 * "should background sync actually run," which must go false the instant the user's own toggle
 * is off, regardless of flavor - see PUBLIC_HOLIDAY_INTEGRATION_PLAN.md ("PRO gating").
 */
class HolidaySettingsGateImpl(
  private val featureManager: FeatureManager,
  private val prefs: Prefs,
) : HolidaySettingsGate {
  override fun isEnabled(): Boolean =
    featureManager.isFeatureEnabled(FeatureManager.Feature.PUBLIC_HOLIDAYS) && prefs.publicHolidaysEnabled

  override fun countryCode(): String = prefs.holidayCountryCode
}
