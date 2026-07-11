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

/**
 * Resolves a [DeepLinkDestination] to its legacy Navigation Component destination id - only for
 * destinations still living in `home_nav.xml`. Everything else has been promoted to typed Nav3
 * entries and is resolved directly by [com.elementary.tasks.home.BottomNavActivity] instead (see
 * `resolveInitialNavKeys`), so they return `null` here.
 */
class ScreenDestinationIdResolver {
  @IdRes
  fun resolve(destination: DeepLinkDestination): Int? =
    when (destination) {
      is DayViewScreen -> null
      is SettingsScreen -> R.id.settingsFragment
      is EditBirthdayScreen -> null
      is ViewBirthdayScreen -> null
      is EditGroupScreen -> null
      is EditPlaceScreen -> null
      is ViewGoogleTaskScreen -> null
      is EditGoogleTaskScreen -> null
      is ViewReminderScreen -> null
      is EditReminderScreen -> null
      is ViewNoteScreen -> null
      is EditNoteScreen -> null
    }
}
