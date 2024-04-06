package com.elementary.tasks.core.os

import android.content.Context

class ContextProvider(context: Context) {

  var context: Context = context.applicationContext
    private set
  var themedContext: Context = context
    private set

  fun switchContext(context: Context) {
    this.context = context.applicationContext
    this.themedContext = context
  }
}
