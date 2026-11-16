package com.elementary.tasks.reminder.dialog

import com.github.naz013.common.ContextProvider
import com.github.naz013.logic.notificationaction.SnoozePickerLauncher

internal class AppSnoozePickerLauncher(
  private val contextProvider: ContextProvider,
) : SnoozePickerLauncher {
  override fun launch(reminderId: String) {
    val context = contextProvider.context
    context.startActivity(SnoozePickerActivity.getLaunchIntent(context, reminderId))
  }
}
