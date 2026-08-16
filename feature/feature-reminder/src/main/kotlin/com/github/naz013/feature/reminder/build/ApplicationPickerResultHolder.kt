package com.github.naz013.feature.reminder.build

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

/**
 * Bridges the package name picked on [BuildReminderNavKey.SelectApplication] back to the `Main`
 * entry once it pops back on top. That's a separate Nav3 entry with its own `ViewModelStoreOwner`,
 * so it can't call back into [BuildReminderViewModel] directly - same constraint the
 * `pendingConfigRefresh` flag works around for [BuildReminderNavKey.Configure] in
 * `BuildReminderNavGraph`, just carrying a value instead of only a boolean.
 */
class ApplicationPickerResultHolder {
  var pendingPackageName: String? = null
}

@Composable
fun rememberApplicationPickerResultHolder(): ApplicationPickerResultHolder = koinInject()
