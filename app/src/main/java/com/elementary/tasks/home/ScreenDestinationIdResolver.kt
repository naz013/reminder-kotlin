package com.elementary.tasks.home

import androidx.annotation.IdRes
import com.elementary.tasks.R
import com.github.naz013.navigation.DayViewScreen
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.EditBirthdayScreen
import com.github.naz013.navigation.EditGoogleTaskScreen
import com.github.naz013.navigation.EditGroupScreen
import com.github.naz013.navigation.EditNoteScreen
import com.github.naz013.navigation.EditPlaceScreen
import com.github.naz013.navigation.EditReminderScreen
import com.github.naz013.navigation.SettingsScreen
import com.github.naz013.navigation.ViewBirthdayScreen
import com.github.naz013.navigation.ViewGoogleTaskScreen
import com.github.naz013.navigation.ViewNoteScreen
import com.github.naz013.navigation.ViewReminderScreen

class ScreenDestinationIdResolver {
  @IdRes
  fun resolve(destination: DeepLinkDestination): Int =
    when (destination) {
      is DayViewScreen -> R.id.dayViewFragment
      is SettingsScreen -> R.id.settingsFragment
      is EditBirthdayScreen -> R.id.birthdayFragment
      is ViewBirthdayScreen -> R.id.birthdayFragment
      is EditGroupScreen -> R.id.groupsFragment
      is EditPlaceScreen -> R.id.placesFragment
      is ViewGoogleTaskScreen -> R.id.actionGoogle
      is EditGoogleTaskScreen -> R.id.actionGoogle
      is ViewReminderScreen -> R.id.previewReminderFragment
      is EditReminderScreen -> R.id.buildReminderFragment
      is ViewNoteScreen -> R.id.actionNotes
      is EditNoteScreen -> R.id.actionNotes
    }
}
