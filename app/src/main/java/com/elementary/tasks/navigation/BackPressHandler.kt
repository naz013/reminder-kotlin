package com.elementary.tasks.navigation

interface BackPressHandler {
  fun canGoBack(): Boolean
}

class DefaultBackPressHandler : BackPressHandler {
  override fun canGoBack(): Boolean = true
}
