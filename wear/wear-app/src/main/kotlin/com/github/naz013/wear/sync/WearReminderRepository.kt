package com.github.naz013.wear.sync

import android.content.Context
import com.github.naz013.wearsync.WearReminderSummary
import com.github.naz013.wearsync.WearSyncProtocol
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * In-memory, process-wide holder for the reminder list synced from the phone. Populated both by
 * an on-launch pull (in case [WearReminderDataListenerService] wasn't running when the phone last
 * pushed) and by that listener service as pushes arrive.
 */
object WearReminderRepository {
  private val _reminders = MutableStateFlow<List<WearReminderSummary>>(emptyList())
  val reminders: StateFlow<List<WearReminderSummary>> = _reminders.asStateFlow()

  suspend fun refreshFromDataClient(context: Context) {
    val dataItems = Wearable.getDataClient(context).dataItems.await()
    try {
      dataItems
        .firstOrNull { it.uri.path == WearSyncProtocol.REMINDERS_DATA_PATH }
        ?.let { applyDataMap(DataMapItem.fromDataItem(it).dataMap) }
    } finally {
      dataItems.release()
    }
  }

  fun applyDataMap(dataMap: DataMap) {
    val items = dataMap.getDataMapArrayList(WearSyncProtocol.KEY_REMINDER_ITEMS).orEmpty()
    _reminders.value = items.map { it.toSummary() }
  }

  private fun DataMap.toSummary(): WearReminderSummary {
    val eventDateTimeMillis = getLong(WearSyncProtocol.KEY_ITEM_EVENT_DATE_TIME_MILLIS, -1L)
    val groupColor = getInt(WearSyncProtocol.KEY_ITEM_GROUP_COLOR, 0)
    return WearReminderSummary(
      uuId = getString(WearSyncProtocol.KEY_ITEM_UU_ID).orEmpty(),
      title = getString(WearSyncProtocol.KEY_ITEM_TITLE).orEmpty(),
      eventDateTimeMillis = eventDateTimeMillis.takeIf { it >= 0L },
      groupColor = groupColor.takeIf { it != 0 },
      canSnooze = getBoolean(WearSyncProtocol.KEY_ITEM_CAN_SNOOZE, true)
    )
  }
}
