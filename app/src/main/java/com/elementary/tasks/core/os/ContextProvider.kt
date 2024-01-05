package com.elementary.tasks.core.os

import android.content.Context

class ContextProvider(context: Context) {

  var context: Context = context.applicationContext
    private set

  fun switchContext(context: Context) {
    this.context = context.applicationContext
  }
}
