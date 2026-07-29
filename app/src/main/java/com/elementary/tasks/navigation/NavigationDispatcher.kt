package com.elementary.tasks.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DataDestination
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.Destination
import com.github.naz013.navigation.DestinationScreen
import com.github.naz013.navigation.EditBirthdayScreen
import com.github.naz013.navigation.EditGoogleTaskScreen
import com.github.naz013.navigation.EditGroupScreen
import com.github.naz013.navigation.EditNoteScreen
import com.github.naz013.navigation.EditPlaceScreen
import com.github.naz013.navigation.EditReminderScreen
import com.github.naz013.navigation.ViewBirthdayScreen
import com.github.naz013.navigation.ViewGoogleTaskScreen
import com.github.naz013.navigation.ViewNoteScreen
import com.github.naz013.navigation.ViewReminderScreen
import com.github.naz013.navigation.intent.IntentDataWriter
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.login.LoginApi

/**
 * Turns a [Destination] published through [Navigator][com.github.naz013.navigation.Navigator]
 * into an Android [Intent] targeting [BottomNavActivity] - the only Activity either destination
 * kind ever resolves to. Exists because publishers (widgets, trampoline activities in other
 * modules) can't depend on [BottomNavActivity] directly.
 */
class NavigationDispatcher(
  private val context: Context,
  private val intentDataWriter: IntentDataWriter,
) {
  fun dispatch(destination: Destination) {
    when (destination) {
      is ActivityDestination -> dispatchActivity(destination)
      is DataDestination -> dispatchData(destination)
    }
  }

  private fun dispatchActivity(destination: ActivityDestination) {
    Logger.i(TAG, "Going to ${destination.screen}")
    if (destination.isLoggedIn && destination.screen != DestinationScreen.Main) {
      LoginApi.openLogged(context, BottomNavActivity::class.java) {
        destination.action?.also { setAction(it) }
        destination.flags?.also { addFlags(it) }
        destination.extras?.also { putExtras(activityExtras(destination.screen, it)) }
      }
    } else {
      context
        .buildIntent(BottomNavActivity::class.java) {
          destination.action?.also { setAction(it) }
          destination.flags?.also { addFlags(it) }
          destination.extras?.also { putExtras(activityExtras(destination.screen, it)) }
        }.also { context.startActivity(it) }
    }
  }

  private fun activityExtras(
    screen: DestinationScreen,
    bundle: Bundle,
  ): Bundle =
    when (screen) {
      DestinationScreen.BirthdayCreate -> withDeepLink(bundle, EditBirthdayScreen(bundle))
      DestinationScreen.BirthdayPreview -> withDeepLink(bundle, ViewBirthdayScreen(bundle))
      DestinationScreen.GoogleTaskPreview -> withDeepLink(bundle, ViewGoogleTaskScreen(bundle))
      DestinationScreen.GoogleTaskCreate -> withDeepLink(bundle, EditGoogleTaskScreen(bundle))
      DestinationScreen.ReminderPreview -> withDeepLink(bundle, ViewReminderScreen(bundle))
      DestinationScreen.ReminderCreate -> withDeepLink(bundle, EditReminderScreen(bundle))
      DestinationScreen.NotePreview -> withDeepLink(bundle, ViewNoteScreen(bundle))
      DestinationScreen.NoteCreate -> withDeepLink(bundle, EditNoteScreen(bundle))
      DestinationScreen.Main -> bundle
    }

  private fun withDeepLink(
    bundle: Bundle,
    deepLinkDestination: DeepLinkDestination,
  ): Bundle = Bundle(bundle).apply { putParcelable(DeepLinkDestination.KEY, deepLinkDestination) }

  private fun dispatchData(destination: DataDestination) {
    val data = destination.data
    if (!isSupported(data)) {
      Logger.e(TAG, "Failed to find destination for the $data")
      return
    }

    Logger.i(TAG, "Going to ${BottomNavActivity::class.java.simpleName}, with $data")

    intentDataWriter.putData(IntentKeys.INTENT_ITEM, data)

    context
      .buildIntent(BottomNavActivity::class.java) {
        setAction(Intent.ACTION_VIEW)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtras(dataExtras(data))
      }.also { context.startActivity(it) }
  }

  private fun isSupported(data: Any): Boolean =
    data is Birthday || data is GroupV2 || data is Place || data is ReminderV2 || data is NoteWithImages

  private fun dataExtras(data: Any): Bundle {
    val deepLinkDestination =
      when (data) {
        is Birthday -> EditBirthdayScreen(deepLinkItemBundle())
        is GroupV2 -> EditGroupScreen(deepLinkItemBundle())
        is Place -> EditPlaceScreen(deepLinkItemBundle())
        is ReminderV2 -> EditReminderScreen(deepLinkItemBundle())
        is NoteWithImages -> EditNoteScreen(Bundle().apply { putBoolean(IntentKeys.INTENT_ITEM, true) })
        else -> error("Unsupported destination data: $data")
      }
    return Bundle().apply { putParcelable(DeepLinkDestination.KEY, deepLinkDestination) }
  }

  private fun deepLinkItemBundle(): Bundle =
    Bundle().apply {
      putBoolean(IntentKeys.INTENT_ITEM, true)
      putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
    }

  companion object {
    private const val TAG = "NavigationDispatcher"
  }
}
