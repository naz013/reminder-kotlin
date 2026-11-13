package com.github.naz013.logic.notificationaction

/**
 * Opens the screen where the user picks when a reminder should be snoozed to.
 */
interface SnoozePickerLauncher {
  fun launch(reminderId: String)
}
