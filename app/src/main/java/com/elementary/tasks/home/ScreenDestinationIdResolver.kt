package com.elementary.tasks.home

import androidx.annotation.IdRes
import com.elementary.tasks.R
import com.github.naz013.navigation.DayViewScreen
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.EditBirthdayScreen
import com.github.naz013.navigation.EditGoogleTaskScreen
import com.github.naz013.navigation.EditGroupScreen
import com.github.naz013.navigation.EditPlaceScreen
import com.github.naz013.navigation.SettingsScreen
import com.github.naz013.navigation.ViewBirthdayScreen
import com.github.naz013.navigation.ViewGoogleTaskScreen
import com.github.naz013.navigation.ViewReminderScreen

class ScreenDestinationIdResolver {

  @IdRes
  fun resolve(destination: DeepLinkDestination): Int {
    return when (destination) {
      is DayViewScreen -> R.id.dayViewFragment
      is SettingsScreen -> R.id.settingsFragment
      is EditBirthdayScreen -> R.id.editBirthdayFragment
      is ViewBirthdayScreen -> R.id.previewBirthdayFragment
      is EditGroupScreen -> R.id.editGroupFragment
      is EditPlaceScreen -> R.id.editPlaceFragment
      is ViewGoogleTaskScreen -> R.id.previewGoogleTaskFragment
      is EditGoogleTaskScreen -> R.id.editGoogleTaskFragment
      is ViewReminderScreen -> R.id.previewReminderFragment
    }
  }
}
