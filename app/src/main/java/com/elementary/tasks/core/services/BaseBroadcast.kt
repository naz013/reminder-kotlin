package com.elementary.tasks.core.services

import android.content.BroadcastReceiver
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.params.Prefs
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseBroadcast : BroadcastReceiver(), KoinComponent {
  protected val prefs by inject<Prefs>()
  protected val notifier by inject<Notifier>()
  protected val appWidgetUpdater by inject<AppWidgetUpdater>()
}
