package com.elementary.tasks.core.utils

import android.content.Context
import androidx.work.WorkManager

class WorkManagerProvider(private val context: Context) {
  fun getWorkManager() = WorkManager.getInstance(context)
}