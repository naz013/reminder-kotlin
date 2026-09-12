package com.github.naz013.wear.sync

import com.github.naz013.wearsync.WearSyncProtocol
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

internal class WearReminderDataListenerService : WearableListenerService() {

  override fun onDataChanged(dataEvents: DataEventBuffer) {
    try {
      dataEvents
        .filter { it.type == DataEvent.TYPE_CHANGED }
        .filter { it.dataItem.uri.path == WearSyncProtocol.REMINDERS_DATA_PATH }
        .forEach { WearReminderRepository.applyDataMap(DataMapItem.fromDataItem(it.dataItem).dataMap) }
    } finally {
      dataEvents.release()
    }
  }
}
