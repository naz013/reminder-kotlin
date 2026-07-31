package com.elementary.tasks.settings.other

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface OtherNavKey : NavKey {
  @Serializable
  data object Other : OtherNavKey

  @Serializable
  data object Permissions : OtherNavKey

  @Serializable
  data object Oss : OtherNavKey

  @Serializable
  data object PrivacyPolicy : OtherNavKey

  @Serializable
  data object Terms : OtherNavKey

  @Serializable
  data object WhatsNew : OtherNavKey

  @Serializable
  data object GeminiFunctions : OtherNavKey
}
