package com.github.naz013.logic.notificationaction

/**
 * Seam over app's `SuperUtil.isPhoneCallActive`, which this module can't depend on (it needs a
 * raw `Context` to reach `AudioManager`). Implemented in `app` by delegating to `SuperUtil` - see
 * `PhoneCallStateProviderImpl`.
 */
fun interface PhoneCallStateProvider {
  fun isPhoneCallActive(): Boolean
}
