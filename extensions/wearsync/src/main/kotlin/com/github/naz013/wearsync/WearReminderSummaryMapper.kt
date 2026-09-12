package com.github.naz013.wearsync

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.google.android.gms.wearable.DataMap

class WearReminderSummaryMapper(
  private val dateTimeManager: DateTimeManager
) {

  fun toSummary(reminder: ReminderV2): WearReminderSummary =
    WearReminderSummary(
      uuId = reminder.uuId,
      title = reminder.summary,
      eventDateTimeMillis = reminder.schedule.eventDateTime
        ?.let { dateTimeManager.utcToLocal(it) }
        ?.let { dateTimeManager.toMillis(it) },
      groupColor = null,
      canSnooze = reminder.places.isEmpty()
    )

  fun toDataMap(summary: WearReminderSummary): DataMap =
    DataMap().apply {
      putString(WearSyncProtocol.KEY_ITEM_UU_ID, summary.uuId)
      putString(WearSyncProtocol.KEY_ITEM_TITLE, summary.title)
      putLong(WearSyncProtocol.KEY_ITEM_EVENT_DATE_TIME_MILLIS, summary.eventDateTimeMillis ?: NO_DUE_DATE)
      putInt(WearSyncProtocol.KEY_ITEM_GROUP_COLOR, summary.groupColor ?: NO_GROUP_COLOR)
      putBoolean(WearSyncProtocol.KEY_ITEM_CAN_SNOOZE, summary.canSnooze)
    }

  companion object {
    private const val NO_DUE_DATE = -1L
    private const val NO_GROUP_COLOR = 0
  }
}
