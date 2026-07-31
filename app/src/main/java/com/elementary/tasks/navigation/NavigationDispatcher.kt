package com.elementary.tasks.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
        destination.extras?.also { applyDeepLink(destination.screen, it) }
      }
    } else {
      context
        .buildIntent(BottomNavActivity::class.java) {
          destination.action?.also { setAction(it) }
          destination.flags?.also { addFlags(it) }
          destination.extras?.also { applyDeepLink(destination.screen, it) }
        }.also { context.startActivity(it) }
    }
  }

  /**
   * Forwards the producer's raw [bundle] as-is (it may already carry its own
   * [DeepLinkDestination.KEY] extra, e.g. a home-screen widget's `fillInIntent`) and, if [screen]
   * implies a typed deep link, adds/overwrites it on top.
   */
  private fun Intent.applyDeepLink(
    screen: DestinationScreen,
    bundle: Bundle,
  ) {
    putExtras(bundle)
    deepLinkDestination(screen, bundle)?.also { putExtra(DeepLinkDestination.KEY, it) }
  }

  private fun deepLinkDestination(
    screen: DestinationScreen,
    bundle: Bundle,
  ): DeepLinkDestination? =
    when (screen) {
      DestinationScreen.BirthdayCreate -> EditBirthdayScreen(id = bundle.getString(IntentKeys.INTENT_ID))
      DestinationScreen.BirthdayPreview -> ViewBirthdayScreen(id = bundle.getString(IntentKeys.INTENT_ID))
      DestinationScreen.GoogleTaskCreate -> EditGoogleTaskScreen
      DestinationScreen.GoogleTaskPreview -> ViewGoogleTaskScreen(id = bundle.getString(IntentKeys.INTENT_ID))
      DestinationScreen.ReminderCreate -> EditReminderScreen(deepLinkText = bundle.getString(Intent.EXTRA_TEXT))
      DestinationScreen.ReminderPreview -> ViewReminderScreen(id = bundle.getString(IntentKeys.INTENT_ID))
      DestinationScreen.NoteCreate ->
        EditNoteScreen(
          sharedText = bundle.getString(Intent.EXTRA_TEXT),
          sharedImageUris = bundle.getUriList(Intent.EXTRA_STREAM)?.map { it.toString() },
        )
      DestinationScreen.NotePreview -> ViewNoteScreen(id = bundle.getString(IntentKeys.INTENT_ID))
      DestinationScreen.Main -> null
    }

  private fun Bundle.getUriList(key: String): ArrayList<Uri>? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      getParcelableArrayList(key, Uri::class.java)
    } else {
      @Suppress("DEPRECATION")
      getParcelableArrayList(key)
    }

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
        putExtra(DeepLinkDestination.KEY, deepLinkDestinationForData(data))
      }.also { context.startActivity(it) }
  }

  private fun isSupported(data: Any): Boolean =
    data is Birthday || data is GroupV2 || data is Place || data is ReminderV2 || data is NoteWithImages

  private fun deepLinkDestinationForData(data: Any): DeepLinkDestination =
    when (data) {
      is Birthday -> EditBirthdayScreen(fromIntentData = true)
      is GroupV2 -> EditGroupScreen(fromIntentData = true)
      is Place -> EditPlaceScreen(fromIntentData = true)
      is ReminderV2 -> EditReminderScreen(fromIntentItem = true)
      is NoteWithImages -> EditNoteScreen(fromIntentData = true)
      else -> error("Unsupported destination data: $data")
    }

  companion object {
    private const val TAG = "NavigationDispatcher"
  }
}
