package com.elementary.tasks.reminder.dialog

data class MoreActionParams(
  var canCancel: Boolean = false,
  var canOpenAttachment: Boolean = false,
  var canSnooze: Boolean = false,
  var canMoveToStatusBar: Boolean = true,
  var canEdit: Boolean = true,
  var canStartAgain: Boolean = false
)
