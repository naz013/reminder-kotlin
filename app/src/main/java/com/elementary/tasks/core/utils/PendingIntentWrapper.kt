package com.elementary.tasks.core.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object PendingIntentWrapper {

  fun getActivity(
    context: Context,
    requestCode: Int,
    intent: Intent,
    flags: Int
  ): PendingIntent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.getActivity(
        context,
        requestCode,
        intent,
        flags and PendingIntent.FLAG_IMMUTABLE
      )
    } else {
      PendingIntent.getActivity(context, requestCode, intent, flags)
    }
  }

  fun getBroadcast(
    context: Context,
    requestCode: Int,
    intent: Intent,
    flags: Int
  ): PendingIntent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        flags and PendingIntent.FLAG_IMMUTABLE
      )
    } else {
      PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }
  }

  fun getService(
    context: Context,
    requestCode: Int,
    intent: Intent,
    flags: Int
  ): PendingIntent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.getService(
        context,
        requestCode,
        intent,
        flags and PendingIntent.FLAG_IMMUTABLE
      )
    } else {
      PendingIntent.getService(context, requestCode, intent, flags)
    }
  }
}