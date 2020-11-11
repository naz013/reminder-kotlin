package com.elementary.tasks.core.services

import android.content.BroadcastReceiver
import com.elementary.tasks.core.utils.Prefs
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
abstract class BaseBroadcast : BroadcastReceiver(), KoinComponent {
  protected val prefs: Prefs by inject()
}