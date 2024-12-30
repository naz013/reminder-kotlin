package com.github.naz013.appwidgets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.android.readSerializable
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.ActivityClass
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.activity.LightThemedActivity
import com.github.naz013.ui.common.context.intentForClass
import com.github.naz013.usecase.googletasks.TasksIntentKeys
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
    if (id != null) {
      bundle.putString(IntentKeys.INTENT_ID, id)
    }
    when (direction) {
      Direction.HOME -> {
        navigator.navigate(
          ActivityDestination(
            activityClass = ActivityClass.Main,
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
            activityClass = ActivityClass.ReminderCreate,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true
          )
        )
      }
      Direction.REMINDER_PREVIEW -> {
        navigator.navigate(
          ActivityDestination(
            activityClass = ActivityClass.ReminderPreview,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true
          )
        )
      }
      Direction.ADD_BIRTHDAY -> {
        navigator.navigate(
          ActivityDestination(
            activityClass = ActivityClass.BirthdayCreate,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true,
            action = Intent.ACTION_VIEW
          )
        )
      }
      Direction.BIRTHDAY_PREVIEW -> {
        navigator.navigate(
          ActivityDestination(
            activityClass = ActivityClass.BirthdayPreview,
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
            activityClass = ActivityClass.NoteCreate,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true
          )
        )
      }
      Direction.NOTE_PREVIEW -> {
        navigator.navigate(
          ActivityDestination(
            activityClass = ActivityClass.NotePreview,
            extras = bundle,
            flags = Intent.FLAG_ACTIVITY_NEW_TASK,
            isLoggedIn = true
          )
        )
      }
      Direction.GOOGLE_TASK -> {
        val action = data?.extra?.get(TasksIntentKeys.INTENT_ACTION) as? String ?: return
        bundle.putString(TasksIntentKeys.INTENT_ACTION, action)
        if (action == TasksIntentKeys.CREATE) {
          navigator.navigate(
            ActivityDestination(
              activityClass = ActivityClass.GoogleTaskCreate,
              extras = bundle,
              flags = Intent.FLAG_ACTIVITY_NEW_TASK,
              isLoggedIn = true
            )
          )
        } else {
          navigator.navigate(
            ActivityDestination(
              activityClass = ActivityClass.GoogleTaskPreview,
              extras = bundle,
              flags = Intent.FLAG_ACTIVITY_NEW_TASK,
              isLoggedIn = true
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
