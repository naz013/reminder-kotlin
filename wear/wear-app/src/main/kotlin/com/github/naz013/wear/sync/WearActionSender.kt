package com.github.naz013.wear.sync

import android.content.Context
import com.github.naz013.logging.Logger
import com.github.naz013.wearsync.WearSyncProtocol
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/** Relays a "done"/"snooze" tap to every connected phone node; the phone side
 * (`WearActionListenerService` in `extensions:wearsync`) forwards it into `ReminderActionProcessor`. */
object WearActionSender {

  suspend fun sendComplete(context: Context, uuId: String) =
    send(context, WearSyncProtocol.ACTION_COMPLETE_PATH, uuId)

  suspend fun sendSnooze(context: Context, uuId: String) =
    send(context, WearSyncProtocol.ACTION_SNOOZE_PATH, uuId)

  private suspend fun send(context: Context, path: String, uuId: String) {
    val payload = uuId.toByteArray(Charsets.UTF_8)
    runCatching {
      val nodes = Wearable.getNodeClient(context).connectedNodes.await()
      nodes.forEach { node ->
        Wearable.getMessageClient(context).sendMessage(node.id, path, payload).await()
      }
    }.onFailure { e ->
      Logger.e(TAG, "Failed to send $path for $uuId", e)
    }
  }

  private const val TAG = "WearActionSender"
}
