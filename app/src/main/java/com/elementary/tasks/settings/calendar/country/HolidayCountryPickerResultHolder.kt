package com.elementary.tasks.settings.calendar.country

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

/**
 * Bridges the country code picked on [com.elementary.tasks.settings.SettingsNavKey.SelectHolidayCountry]
 * back to the Calendar settings entry once it pops back on top - that's a separate Nav3 entry with
 * its own `ViewModelStoreOwner`, so it can't call back into `CalendarSettingsViewModel` directly.
 * Mirrors `ApplicationPickerResultHolder`'s approach to the same constraint.
 */
class HolidayCountryPickerResultHolder {
  var pendingCountryCode: String? = null
}

@Composable
fun rememberHolidayCountryPickerResultHolder(): HolidayCountryPickerResultHolder = koinInject()
