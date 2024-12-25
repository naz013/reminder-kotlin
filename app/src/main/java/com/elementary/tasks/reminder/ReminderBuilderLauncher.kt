package com.elementary.tasks.reminder

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.build.BuildReminderActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.github.naz013.ui.common.activity.DeepLinkData
import com.github.naz013.ui.common.context.intentForClass
import com.github.naz013.ui.common.login.LoginApi

class ReminderBuilderLauncher(private val prefs: Prefs) {

  init {
    PENDING_INTENT_CLASS = getActivityClass()
  }

  fun openNotLogged(context: Context, builder: Intent.() -> Unit) {
    val intent = context.intentForClass(getActivityClass()).apply {
      builder(this)
    }
    context.startActivity(intent)
  }

  fun openLogged(context: Context, builder: Intent.() -> Unit) {
    LoginApi.openLogged(context, getActivityClass(), builder)
  }

  fun toggleBuilder(activity: Activity) {
    prefs.useLegacyBuilder = !prefs.useLegacyBuilder
    PENDING_INTENT_CLASS = getActivityClass()
    LoginApi.openLogged(activity, getActivityClass()) { }
    activity.finish()
  }

  fun openDeepLink(
    context: Context,
    deepLinkData: DeepLinkData,
    builder: Intent.() -> Unit
  ) {
    LoginApi.openLogged(context, getActivityClass(), deepLinkData, builder)
  }

  fun getActivityClass(): Class<*> {
    return if (prefs.useLegacyBuilder) {
      CreateReminderActivity::class.java
    } else {
      BuildReminderActivity::class.java
    }
  }

  companion object {
    var PENDING_INTENT_CLASS: Class<*> = BuildReminderActivity::class.java
  }
}
