package com.elementary.tasks.core.services

import android.content.Context
import android.content.Intent
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.logic.habit.nudge.HabitNudgeProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class HabitActionReceiver : BaseBroadcast() {
  private val habitNudgeProcessor by inject<HabitNudgeProcessor>()
  private val dispatcherProvider by inject<DispatcherProvider>()

  override fun onReceive(
    context: Context,
    intent: Intent?,
  ) {
    val id = intent?.getStringExtra(IntentKeys.INTENT_ID)
    Logger.d(TAG, "onReceive: ${intent?.action}, id=$id")
    if (intent?.action != ACTION_CHECK_IN || id.isNullOrEmpty()) return
    val pendingResult = goAsync()
    CoroutineScope(dispatcherProvider.default()).launch {
      try {
        habitNudgeProcessor.onCheckIn(id)
      } finally {
        pendingResult.finish()
      }
    }
  }

  companion object {
    private const val TAG = "HabitActionReceiver"
    const val ACTION_CHECK_IN = "com.elementary.tasks.habit.ACTION_CHECK_IN"
  }
}
