package com.elementary.tasks.core.analytics

import com.backdoor.engine.Model
import com.backdoor.engine.misc.ActionType
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.AnalyticsTracker
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.analytics.VoiceFeatureUsedEvent

class VoiceAnalyticsTracker(
  private val analyticsEventSender: AnalyticsEventSender
) : AnalyticsTracker() {

  fun screenOpened() {
    analyticsEventSender.send(ScreenUsedEvent(Screen.VOICE_CONTROL))
  }

  fun sendEvent(lang: Int, status: Status, model: Model? = null) {
    analyticsEventSender.send(
      VoiceFeatureUsedEvent(
        getLanguage(lang),
        getStatusString(status),
        getActionString(model)
      )
    )
  }

  private fun getActionString(model: Model?): String {
    return if (model == null) {
      "no_action"
    } else {
      when (model.type) {
        ActionType.REMINDER -> "create_reminder"
        ActionType.NOTE -> "create_note"
        ActionType.ANSWER -> "yes_no"
        ActionType.SHOW -> "show"
        ActionType.GROUP -> "create_group"
        ActionType.ACTION -> "open"
        else -> "no_action"
      }
    }
  }

  private fun getLanguage(lang: Int): String {
    return when (lang) {
      0 -> "english"
      1 -> "ukrainian"
      2 -> "spanish"
      3 -> "portuguese"
      4 -> "polish"
      else -> "english_error"
    }
  }

  private fun getStatusString(status: Status): String {
    return if (status == Status.SUCCESS) {
      "success"
    } else {
      "fail"
    }
  }
}

enum class Status {
  SUCCESS, FAIL
}
