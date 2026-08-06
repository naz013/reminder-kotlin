package com.github.naz013.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class DeepLinkDestination : Parcelable {

  companion object {
    const val KEY = "deep_link_destination"
  }
}

@Parcelize
data class DayViewScreen(
  val dateMillis: Long
) : DeepLinkDestination()

@Parcelize
data class EditBirthdayScreen(
  val id: String? = null,
  val fromIntentData: Boolean = false
) : DeepLinkDestination()

@Parcelize
data class ViewBirthdayScreen(
  val id: String? = null
) : DeepLinkDestination()

@Parcelize
data object SettingsScreen : DeepLinkDestination()

@Parcelize
data class EditGroupScreen(
  val id: String? = null,
  val fromIntentData: Boolean = false
) : DeepLinkDestination()

@Parcelize
data class EditPlaceScreen(
  val id: String? = null,
  val fromIntentData: Boolean = false
) : DeepLinkDestination()

@Parcelize
data class ViewGoogleTaskScreen(
  val id: String? = null
) : DeepLinkDestination()

@Parcelize
data object EditGoogleTaskScreen : DeepLinkDestination()

@Parcelize
data class ViewReminderScreen(
  val id: String? = null
) : DeepLinkDestination()

@Parcelize
data class EditReminderScreen(
  val fromIntentItem: Boolean = false,
  val deepLinkText: String? = null
) : DeepLinkDestination()

@Parcelize
data class ViewNoteScreen(
  val id: String? = null
) : DeepLinkDestination()

@Parcelize
data class EditNoteScreen(
  val id: String? = null,
  val fromIntentData: Boolean = false,
  val sharedText: String? = null,
  val sharedImageUris: List<String>? = null
) : DeepLinkDestination()
