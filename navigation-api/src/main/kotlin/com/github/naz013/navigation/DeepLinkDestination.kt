package com.github.naz013.navigation

import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class DeepLinkDestination : Parcelable {

  companion object {
    const val KEY = "deep_link_destination"
  }
}

@Parcelize
data class DayViewScreen(
  val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data class EditBirthdayScreen(
  val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data class ViewBirthdayScreen(
  val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data object SettingsScreen : DeepLinkDestination()

@Parcelize
data class EditGroupScreen(
  val extras: Bundle
) : DeepLinkDestination()
