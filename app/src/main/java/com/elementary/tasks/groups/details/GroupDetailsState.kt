package com.elementary.tasks.groups.details

import com.github.naz013.ui.notification.settings.NotificationOverrideSubtitles
import com.github.naz013.feature.reminder.lists.data.UiReminderList

data class GroupDetailsState(
  val isLoading: Boolean = true,
  val title: String = "",
  val color: Int = 0,
  val canDelete: Boolean = false,
  val notificationSubtitles: NotificationOverrideSubtitles = NotificationOverrideSubtitles(),
  val reminders: List<UiReminderList> = emptyList(),
)
