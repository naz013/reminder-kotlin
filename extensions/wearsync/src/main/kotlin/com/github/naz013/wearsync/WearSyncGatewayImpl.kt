package com.github.naz013.wearsync

import android.content.Context
import com.github.naz013.logging.Logger
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

internal class WearSyncGatewayImpl(
  private val context: Context,
  private val mapper: WearReminderSummaryMapper
) : WearSyncGateway {

  override suspend fun pushReminders(reminders: List<WearReminderSummary>) {
    val request = PutDataMapRequest.create(WearSyncProtocol.REMINDERS_DATA_PATH).apply {
      dataMap.putDataMapArrayList(
        WearSyncProtocol.KEY_REMINDER_ITEMS,
        ArrayList(reminders.map { mapper.toDataMap(it) })
      )
      setUrgent()
    }
    runCatching {
      Wearable.getDataClient(context).putDataItem(request.asPutDataRequest()).await()
    }.onFailure { e ->
      Logger.e(TAG, "Failed to push ${reminders.size} reminders to Wear", e)
    }
  }

  companion object {
    private const val TAG = "WearSyncGateway"
  }
}
