package com.github.naz013.appwidgets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.android.readSerializable
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DestinationScreen
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.activity.LightThemedActivity
import com.github.naz013.ui.common.context.intentForClass
import org.koin.android.ext.android.inject

internal class AppWidgetActionActivity : LightThemedActivity() {

  private val navigator by inject<Navigator>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    intent.also {
      Logger.i(
        "AppWidgetActionActivity",
        "Received intent with keys = ${it.extras?.keySet()?.joinToString()}"
      )
    }
    val direction = intent.readSerializable(DIRECTION, Direction::class.java)
    Logger.i("AppWidgetActionActivity", "Received direction = $direction")
    if (direction == null) {
      finish()
      return
    }
    val data = intent.readSerializable(DATA, WidgetIntentProtocol::class.java)
    Logger.d("AppWidgetActionActivity", "Received data = $data")
    val id = data?.extra?.get(IntentKeys.INTENT_ID) as? String
    val bundle = intent.extras ?: Bundle()
    when (direction) {
      Direction.HOME -> {
        navigator.navigate(
          ActivityDestination(
            screen = DestinationScreen.Main,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true,
            action = Intent.ACTION_VIEW
          )
        )
      }
      Direction.ADD_REMINDER -> {
        navigator.navigate(
          ActivityDestination(
            screen = DestinationScreen.ReminderCreate,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true,
            action = Intent.ACTION_VIEW
          )
        )
      }
      Direction.REMINDER_PREVIEW -> {
        if (id != null) {
          bundle.putString(IntentKeys.INTENT_ID, id)
        }
        navigator.navigate(
          ActivityDestination(
            screen = DestinationScreen.ReminderPreview,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true,
            action = Intent.ACTION_VIEW
          )
        )
      }
      Direction.ADD_BIRTHDAY -> {
        navigator.navigate(
          ActivityDestination(
            screen = DestinationScreen.BirthdayCreate,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true,
            action = Intent.ACTION_VIEW
          )
        )
      }
      Direction.BIRTHDAY_PREVIEW -> {
        if (id != null) {
          bundle.putString(IntentKeys.INTENT_ID, id)
        }
        navigator.navigate(
          ActivityDestination(
            screen = DestinationScreen.BirthdayPreview,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true,
            action = Intent.ACTION_VIEW
          )
        )
      }
      Direction.ADD_NOTE -> {
        navigator.navigate(
          ActivityDestination(
            screen = DestinationScreen.NoteCreate,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true
          )
        )
      }
      Direction.NOTE_PREVIEW -> {
        if (id != null) {
          bundle.putString(IntentKeys.INTENT_ID, id)
        }
        navigator.navigate(
          ActivityDestination(
            screen = DestinationScreen.NotePreview,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true
          )
        )
      }
      Direction.GOOGLE_TASK -> {
        if (id.isNullOrEmpty()) {
          navigator.navigate(
            ActivityDestination(
              screen = DestinationScreen.GoogleTaskCreate,
              extras = bundle,
              flags = Intent.FLAG_ACTIVITY_NEW_TASK,
              isLoggedIn = true,
              action = Intent.ACTION_VIEW
            )
          )
        } else {
          bundle.putString(IntentKeys.INTENT_ID, id)
          navigator.navigate(
            ActivityDestination(
              screen = DestinationScreen.GoogleTaskPreview,
              extras = bundle,
              flags = Intent.FLAG_ACTIVITY_NEW_TASK,
              isLoggedIn = true,
              action = Intent.ACTION_VIEW
            )
          )
        }
      }
    }
    finish()
  }

  override fun requireLogin(): Boolean {
    return false
  }

  companion object {

    const val DIRECTION = "arg_direction"
    const val DATA = "arg_data"

    fun createIntent(context: Context): Intent {
      return context.intentForClass(AppWidgetActionActivity::class.java)
    }
  }
}
