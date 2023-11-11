package com.elementary.tasks.calendar.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.MainThread
import androidx.localbroadcastmanager.content.LocalBroadcastManager

typealias CalendarDataEngineBroadcastCallback = () -> Unit

class CalendarDataEngineBroadcast(
  private val context: Context
) {

  private val receiverMap = mutableMapOf<String, BroadcastReceiver>()

  @MainThread
  fun sendEvent() {
    val intent = Intent().apply {
      action = EVENT_READY
    }
    getBroadcastManager().sendBroadcast(intent)
  }

  @MainThread
  fun observerEvent(
    parent: String,
    action: String,
    callback: CalendarDataEngineBroadcastCallback
  ) {
    val broadcastReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (action == intent?.action) {
          callback()
        }
      }
    }
    val intentFilter = IntentFilter(action)
    getBroadcastManager().registerReceiver(broadcastReceiver, intentFilter)
    receiverMap[parent] = broadcastReceiver
  }

  @MainThread
  fun removeObserver(parent: String) {
    receiverMap[parent]?.also {
      getBroadcastManager().unregisterReceiver(it)
    }
  }

  private fun getBroadcastManager(): LocalBroadcastManager {
    return LocalBroadcastManager.getInstance(context)
  }

  companion object {
    const val EVENT_READY = "com.calendar.data.READY"
    private const val TAG = "CalendarDataEngineBroadcast"
  }
}
