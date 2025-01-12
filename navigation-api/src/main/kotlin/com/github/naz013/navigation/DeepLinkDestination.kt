package com.github.naz013.navigation

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed class DeepLinkDestination : Parcelable {

  abstract val extras: Bundle?

  companion object {
    const val KEY = "deep_link_destination"
  }
}

@Parcelize
data class DayViewScreen(
  override val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data class EditBirthdayScreen(
  override val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data class ViewBirthdayScreen(
  override val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data object SettingsScreen : DeepLinkDestination() {
  @IgnoredOnParcel
  override val extras: Bundle? = null
}

@Parcelize
data class EditGroupScreen(
  override val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data class EditPlaceScreen(
  override val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data class ViewGoogleTaskScreen(
  override val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data class EditGoogleTaskScreen(
  override val extras: Bundle
) : DeepLinkDestination()
