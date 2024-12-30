package com.elementary.tasks.navigation

import android.content.Context
import android.os.Bundle
import com.elementary.tasks.googletasks.preview.GoogleTaskPreviewActivity
import com.elementary.tasks.googletasks.task.GoogleTaskActivity
import com.elementary.tasks.home.BottomNavActivity
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.reminder.build.BuildReminderActivity
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.ActivityClass
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.FragmentEditBirthday
import com.github.naz013.navigation.FragmentViewBirthday
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.login.LoginApi

class ActivityNavigator(
  private val context: Context
) {

  fun navigate(activityDestination: ActivityDestination) {
    Logger.i("ActivityNavigator", "Going to ${activityDestination.activityClass}")
    val clazz = getClass(activityDestination.activityClass)
    if (activityDestination.isLoggedIn && activityDestination.activityClass != ActivityClass.Main) {
      LoginApi.openLogged(context, clazz) {
        activityDestination.action?.also { setAction(it) }
        activityDestination.flags?.also { addFlags(it) }
        activityDestination.extras?.also {
          putExtras(getExtras(activityDestination.activityClass, it))
        }
      }
    } else {
      context.buildIntent(clazz) {
        activityDestination.action?.also { setAction(it) }
        activityDestination.flags?.also { addFlags(it) }
        activityDestination.extras?.also {
          putExtras(getExtras(activityDestination.activityClass, it))
        }
      }.also {
        context.startActivity(it)
      }
    }
  }

  private fun getExtras(activityClass: ActivityClass, bundle: Bundle?): Bundle {
    if (bundle == null) return Bundle()
    return when (activityClass) {
      ActivityClass.BirthdayCreate -> {
        val deepLinkDestination = FragmentEditBirthday(bundle)
        Bundle(bundle).apply {
          putParcelable(DeepLinkDestination.KEY, deepLinkDestination)
        }
      }
      ActivityClass.BirthdayPreview -> {
        val deepLinkDestination = FragmentViewBirthday(bundle)
        Bundle(bundle).apply {
          putParcelable(DeepLinkDestination.KEY, deepLinkDestination)
        }
      }
      else -> bundle
    }
  }

  private fun getClass(activityClass: ActivityClass): Class<*> {
    return when (activityClass) {
      ActivityClass.ReminderPreview -> ReminderPreviewActivity::class.java
      ActivityClass.ReminderCreate -> BuildReminderActivity::class.java
      ActivityClass.NotePreview -> NotePreviewActivity::class.java
      ActivityClass.NoteCreate -> CreateNoteActivity::class.java
      ActivityClass.BirthdayPreview -> BottomNavActivity::class.java
      ActivityClass.BirthdayCreate -> BottomNavActivity::class.java
      ActivityClass.GoogleTaskPreview -> GoogleTaskPreviewActivity::class.java
      ActivityClass.GoogleTaskCreate -> GoogleTaskActivity::class.java
      ActivityClass.Main -> BottomNavActivity::class.java
    }
  }
}
