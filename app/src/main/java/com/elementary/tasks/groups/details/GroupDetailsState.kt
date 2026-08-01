package com.elementary.tasks.groups.details

import com.elementary.tasks.groups.NotificationOverrideSubtitles
import com.elementary.tasks.reminder.lists.data.UiReminderList

data class GroupDetailsState(
  val isLoading: Boolean = true,
  val title: String = "",
  val color: Int = 0,
  val canDelete: Boolean = false,
  val notificationSubtitles: NotificationOverrideSubtitles = NotificationOverrideSubtitles(),
  val reminders: List<UiReminderList> = emptyList(),
)
