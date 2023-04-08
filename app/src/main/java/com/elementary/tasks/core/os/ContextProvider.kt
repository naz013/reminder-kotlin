package com.elementary.tasks.core.os

import android.content.Context

class ContextProvider(context: Context) {
  val context: Context = context.applicationContext
}
