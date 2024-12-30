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
data class FragmentDayView(
  val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data class FragmentEditBirthday(
  val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data class FragmentViewBirthday(
  val extras: Bundle
) : DeepLinkDestination()

@Parcelize
data object FragmentSettings : DeepLinkDestination()
