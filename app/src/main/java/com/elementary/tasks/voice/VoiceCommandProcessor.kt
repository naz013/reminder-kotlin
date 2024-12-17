package com.elementary.tasks.voice

import com.backdoor.engine.Model
import com.backdoor.engine.Recognizer
import com.backdoor.engine.misc.Action
import com.elementary.tasks.core.analytics.Status
import com.elementary.tasks.core.analytics.VoiceAnalyticsTracker
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.github.naz013.logging.Logger
import org.apache.commons.lang3.StringUtils

class VoiceCommandProcessor(
  private val dateTimeManager: DateTimeManager,
  private val prefs: Prefs,
  private val recognizer: Recognizer,
  private val voiceAnalyticsTracker: VoiceAnalyticsTracker,
  private val contactsHelper: ContactsHelper
) {

  operator fun invoke(matches: List<String>): ProcessResult {
    return if (matches.isNotEmpty()) {
      val model = findResults(matches)
      if (model != null) {
        ProcessResult.ReminderSuccess(model)
      } else {
        val text = matches[0]
        ProcessResult.TextSuccess(StringUtils.capitalize(text))
      }
    } else {
      ProcessResult.Error
    }
  }

  private fun findResults(matches: List<*>): Reminder? {
    recognizer.setContactHelper(contactsHelper)
    for (i in matches.indices) {
      val key = matches[i]
      val keyStr = key.toString()
      val model = runCatching { recognizer.recognize(keyStr) }.getOrNull()
      if (model != null) {
        Logger.d("findResults: $model")
        voiceAnalyticsTracker.sendEvent(prefs.voiceLocale, Status.SUCCESS, model)
        return createReminder(model)
      }
    }
    return null
  }

  private fun createReminder(model: Model): Reminder? {
    val action = model.action
    val weekdays = model.weekdays
    var eventTime = dateTimeManager.getFromGmtVoiceEngine(model.dateTime) ?: return null
    var typeT = Reminder.BY_DATE
    if (action == Action.WEEK || action == Action.WEEK_CALL || action == Action.WEEK_SMS) {
      typeT = Reminder.BY_WEEK
      eventTime = dateTimeManager.getNextWeekdayTime(eventTime, weekdays, 0)
      if (model.target.isNullOrEmpty()) {
        typeT = if (action == Action.WEEK_CALL) {
          Reminder.BY_WEEK_CALL
        } else {
          Reminder.BY_WEEK_SMS
        }
      }
    } else if (action == Action.CALL) {
      typeT = Reminder.BY_DATE_CALL
    } else if (action == Action.MESSAGE) {
      typeT = Reminder.BY_DATE_SMS
    } else if (action == Action.MAIL) {
      typeT = Reminder.BY_DATE_EMAIL
    }

    val isCal = prefs.getBoolean(PrefsConstants.EXPORT_TO_CALENDAR)
    val isStock = prefs.getBoolean(PrefsConstants.EXPORT_TO_STOCK)

    val reminder = Reminder()
    reminder.type = typeT
    reminder.summary = model.summary
    reminder.weekdays = weekdays
    reminder.repeatInterval = model.repeatInterval
    reminder.after = model.afterMillis
    reminder.target = model.target ?: ""
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(eventTime)
    reminder.startTime = dateTimeManager.getGmtFromDateTime(eventTime)
    reminder.exportToCalendar = model.hasCalendar && (isCal || isStock)
    Logger.d("createReminder: $reminder")
    return reminder
  }

  sealed class ProcessResult {
    data class ReminderSuccess(val reminder: Reminder) : ProcessResult()
    data class TextSuccess(val text: String) : ProcessResult()
    data object Error : ProcessResult()
  }
}
