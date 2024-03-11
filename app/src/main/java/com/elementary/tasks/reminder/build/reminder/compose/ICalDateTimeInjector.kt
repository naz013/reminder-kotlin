package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.elementary.tasks.reminder.build.reminder.ICalDateTimeCalculator
import org.threeten.bp.LocalDateTime

class ICalDateTimeInjector(
  private val iCalDateTimeCalculator: ICalDateTimeCalculator,
  private val dateTimeManager: DateTimeManager
) {

  operator fun invoke(
    reminder: Reminder,
    processedBuilderItems: ProcessedBuilderItems
  ): LocalDateTime? {
    val eventData = iCalDateTimeCalculator(processedBuilderItems) ?: return null
    Traces.d(TAG, "invoke: eventData = $eventData")

    val startTime = eventData.startDateTime
    Traces.d(TAG, "invoke: startTime = $startTime, recurObject = ${eventData.recurObject}")

    reminder.recurDataObject = eventData.recurObject
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)

    return eventData.startDateTime
  }

  companion object {
    private const val TAG = "ICalDateTimeInjector"
  }
}
