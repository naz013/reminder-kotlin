package com.elementary.tasks.navigation

import android.content.Context
import android.os.Bundle
import com.elementary.tasks.home.BottomNavActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.reminder.build.BuildReminderActivity
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.DestinationScreen
import com.github.naz013.navigation.EditBirthdayScreen
import com.github.naz013.navigation.EditGoogleTaskScreen
import com.github.naz013.navigation.ViewBirthdayScreen
import com.github.naz013.navigation.ViewGoogleTaskScreen
import com.github.naz013.navigation.ViewReminderScreen
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.login.LoginApi

class ActivityNavigationDispatcher(
  private val context: Context
) : NavigationDispatcher<ActivityDestination> {

  override fun dispatch(destination: ActivityDestination) {
    Logger.i("ActivityNavigator", "Going to ${destination.screen}")
    val clazz = getClass(destination.screen)
    if (destination.isLoggedIn && destination.screen != DestinationScreen.Main) {
      LoginApi.openLogged(context, clazz) {
        destination.action?.also { setAction(it) }
        destination.flags?.also { addFlags(it) }
        destination.extras?.also {
          putExtras(getExtras(destination.screen, it))
        }
      }
    } else {
      context.buildIntent(clazz) {
        destination.action?.also { setAction(it) }
        destination.flags?.also { addFlags(it) }
        destination.extras?.also {
          putExtras(getExtras(destination.screen, it))
        }
      }.also {
        context.startActivity(it)
      }
    }
  }

  private fun getExtras(destinationScreen: DestinationScreen, bundle: Bundle?): Bundle {
    if (bundle == null) return Bundle()
    return when (destinationScreen) {
      DestinationScreen.BirthdayCreate -> {
        val deepLinkDestination = EditBirthdayScreen(bundle)
        Bundle(bundle).apply {
          putParcelable(DeepLinkDestination.KEY, deepLinkDestination)
        }
      }

      DestinationScreen.BirthdayPreview -> {
        val deepLinkDestination = ViewBirthdayScreen(bundle)
        Bundle(bundle).apply {
          putParcelable(DeepLinkDestination.KEY, deepLinkDestination)
        }
      }

      DestinationScreen.GoogleTaskPreview -> {
        val deepLinkDestination = ViewGoogleTaskScreen(bundle)
        Bundle(bundle).apply {
          putParcelable(DeepLinkDestination.KEY, deepLinkDestination)
        }
      }

      DestinationScreen.GoogleTaskCreate -> {
        val deepLinkDestination = EditGoogleTaskScreen(bundle)
        Bundle(bundle).apply {
          putParcelable(DeepLinkDestination.KEY, deepLinkDestination)
        }
      }

      DestinationScreen.ReminderPreview -> {
        val deepLinkDestination = ViewReminderScreen(bundle)
        Bundle(bundle).apply {
          putParcelable(DeepLinkDestination.KEY, deepLinkDestination)
        }
      }

      else -> bundle
    }
  }

  private fun getClass(destinationScreen: DestinationScreen): Class<*> {
    return when (destinationScreen) {
      DestinationScreen.ReminderPreview -> BottomNavActivity::class.java
      DestinationScreen.ReminderCreate -> BuildReminderActivity::class.java
      DestinationScreen.NotePreview -> NotePreviewActivity::class.java
      DestinationScreen.NoteCreate -> CreateNoteActivity::class.java
      DestinationScreen.BirthdayPreview -> BottomNavActivity::class.java
      DestinationScreen.BirthdayCreate -> BottomNavActivity::class.java
      DestinationScreen.GoogleTaskPreview -> BottomNavActivity::class.java
      DestinationScreen.GoogleTaskCreate -> BottomNavActivity::class.java
      DestinationScreen.Main -> BottomNavActivity::class.java
    }
  }
}
