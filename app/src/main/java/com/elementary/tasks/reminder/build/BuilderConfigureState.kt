package com.elementary.tasks.reminder.build

data class BuilderConfigureState(
  val isSummaryChecked: Boolean = false,
  val isBeforeChecked: Boolean = false,
  val isRepeatChecked: Boolean = false,
  val isRepeatLimitChecked: Boolean = false,
  val isPriorityChecked: Boolean = false,
  val isAttachmentChecked: Boolean = false,
  val isCalendarChecked: Boolean = false,
  val isTasksChecked: Boolean = false,
  val isTasksRowVisible: Boolean = false,
  val isExtraChecked: Boolean = false,
  val isLedChecked: Boolean = false,
  val isLedRowVisible: Boolean = false,
  val isICalendarChecked: Boolean = false,
  val isICalendarRowVisible: Boolean = false,
  val isMakeCallChecked: Boolean = false,
  val isSendSmsChecked: Boolean = false,
  val isOpenAppChecked: Boolean = false,
  val isOpenLinkChecked: Boolean = false,
  val isSendEmailChecked: Boolean = false,
)
