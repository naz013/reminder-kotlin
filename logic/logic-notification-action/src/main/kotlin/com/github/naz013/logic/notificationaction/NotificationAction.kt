package com.github.naz013.logic.notificationaction

import android.content.Intent

/**
 * @param activityIntent when set, the action opens this activity directly instead of sending a
 * broadcast with [actionKey] - needed because Android 12+ blocks starting activities from a
 * broadcast receiver triggered by a notification tap.
 */
data class NotificationAction(
  val icon: Int,
  val label: CharSequence,
  val actionKey: String,
  val activityIntent: Intent? = null,
)
