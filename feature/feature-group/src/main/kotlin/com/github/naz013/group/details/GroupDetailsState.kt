package com.github.naz013.group.details

import com.github.naz013.ui.notification.settings.NotificationOverrideSubtitles
import com.github.naz013.ui.reminder.UiReminderList

internal data class GroupDetailsState(
  val isLoading: Boolean = true,
  val title: String = "",
  val color: Int = 0,
  val canDelete: Boolean = false,
  val notificationSubtitles: NotificationOverrideSubtitles = NotificationOverrideSubtitles(),
  val reminders: List<UiReminderList> = emptyList(),
)
