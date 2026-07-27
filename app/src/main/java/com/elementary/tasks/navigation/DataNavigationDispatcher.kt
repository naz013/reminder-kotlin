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
import com.github.naz013.navigation.DataDestination
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.EditBirthdayScreen
import com.github.naz013.navigation.EditGroupScreen
import com.github.naz013.navigation.EditNoteScreen
import com.github.naz013.navigation.EditPlaceScreen
import com.github.naz013.navigation.EditReminderScreen
import com.github.naz013.navigation.intent.IntentDataWriter
import com.github.naz013.ui.common.context.buildIntent

class DataNavigationDispatcher(
  private val context: Context,
  private val intentDataWriter: IntentDataWriter,
) : NavigationDispatcher<DataDestination> {
  override fun dispatch(destination: DataDestination) {
    val data = destination.data

    val clazz = getClass(data)
    if (clazz == null) {
      Logger.e("DataNavigationDispatcher", "Failed to find destination for the $data")
      return
    }

    Logger.i("DataNavigationDispatcher", "Going to ${clazz.simpleName}, with $data")

    intentDataWriter.putData(IntentKeys.INTENT_ITEM, data)

    context
      .buildIntent(clazz) {
        getAction(data)?.also { setAction(it) }
        getFlags(data)?.also { addFlags(it) }
        getExtras(data)?.also { putExtras(it) }
      }.also {
        context.startActivity(it)
      }
  }

  private fun getExtras(data: Any): Bundle? =
    when (data) {
      is Birthday -> {
        Bundle().apply {
          putParcelable(
            DeepLinkDestination.KEY,
            EditBirthdayScreen(
              Bundle().apply {
                putBoolean(IntentKeys.INTENT_ITEM, true)
                putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
              },
            ),
          )
        }
      }

      is GroupV2 -> {
        Bundle().apply {
          putParcelable(
            DeepLinkDestination.KEY,
            EditGroupScreen(
              Bundle().apply {
                putBoolean(IntentKeys.INTENT_ITEM, true)
                putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
              },
            ),
          )
        }
      }

      is Place -> {
        Bundle().apply {
          putParcelable(
            DeepLinkDestination.KEY,
            EditPlaceScreen(
              Bundle().apply {
                putBoolean(IntentKeys.INTENT_ITEM, true)
                putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
              },
            ),
          )
        }
      }

      is ReminderV2 -> {
        Bundle().apply {
          putParcelable(
            DeepLinkDestination.KEY,
            EditReminderScreen(
              Bundle().apply {
                putBoolean(IntentKeys.INTENT_ITEM, true)
                putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
              },
            ),
          )
        }
      }

      is NoteWithImages -> {
        Bundle().apply {
          putParcelable(
            DeepLinkDestination.KEY,
            EditNoteScreen(
              Bundle().apply {
                putBoolean(IntentKeys.INTENT_ITEM, true)
              },
            ),
          )
        }
      }

      else -> null
    }

  private fun getFlags(data: Any): Int? = Intent.FLAG_ACTIVITY_NEW_TASK

  private fun getAction(data: Any): String? =
    when (data) {
      is Birthday, is GroupV2, is Place, is ReminderV2, is NoteWithImages -> Intent.ACTION_VIEW
      else -> null
    }

  private fun getClass(data: Any): Class<*>? =
    when (data) {
      is Birthday -> BottomNavActivity::class.java
      is ReminderV2 -> BottomNavActivity::class.java
      is GroupV2 -> BottomNavActivity::class.java
      is NoteWithImages -> BottomNavActivity::class.java
      is Place -> BottomNavActivity::class.java
      else -> null
    }
}
