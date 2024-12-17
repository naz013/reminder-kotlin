package com.elementary.tasks.reminder.build.reminder.compose

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.bi.ProcessedBuilderItems
import com.elementary.tasks.reminder.build.reminder.ICalDateTimeCalculator
import com.github.naz013.logging.Logger
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
    Logger.d("invoke: eventData = $eventData")

    val startTime = eventData.startDateTime
    Logger.d("invoke: startTime = $startTime, recurObject = ${eventData.recurObject}")

    reminder.recurDataObject = eventData.recurObject
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)

    return eventData.startDateTime
  }
}
