package com.github.naz013.logic.reminder.query

import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.resolve
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderSettingsRepository

class ResolveReminderV2NotificationSettingsUseCase(
  private val groupV2Repository: GroupV2Repository,
  private val reminderSettingsRepository: ReminderSettingsRepository
) {

  suspend operator fun invoke(reminder: ReminderV2): NotificationSettings {
    val group = reminder.groupId?.let { groupV2Repository.getById(it) }
    return reminder.notification.resolve(
      group = group?.notification,
      defaults = reminderSettingsRepository.getNotificationDefaults()
    )
  }
}
