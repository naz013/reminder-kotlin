package com.elementary.tasks.groups

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.IdProvider
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.repository.ReminderGroupRepository
import java.util.Random

class GroupsUtil(
  private val textProvider: TextProvider,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val dateTimeManager: DateTimeManager,
  private val idProvider: IdProvider
) {

  private val random = Random()

  suspend fun initDefaultIfEmpty() {
    if (reminderGroupRepository.getAll().isEmpty()) {
      initDefault()
    }
  }

  private suspend fun initDefault(): String {
    val def = ReminderGroup(
      groupTitle = textProvider.getText(R.string.general),
      groupColor = random.nextInt(16),
      groupDateTime = dateTimeManager.getNowGmtDateTime(),
      isDefaultGroup = true,
      groupUuId = idProvider.generateUuid()
    )
    runCatching {
      reminderGroupRepository.save(def)
      reminderGroupRepository.save(
        ReminderGroup(
          groupTitle = textProvider.getText(R.string.work),
          groupColor = random.nextInt(16),
          groupDateTime = dateTimeManager.getNowGmtDateTime(),
          isDefaultGroup = false,
          groupUuId = idProvider.generateUuid()
        )
      )
      reminderGroupRepository.save(
        ReminderGroup(
          groupTitle = textProvider.getText(R.string.personal),
          groupColor = random.nextInt(16),
          groupDateTime = dateTimeManager.getNowGmtDateTime(),
          isDefaultGroup = false,
          groupUuId = idProvider.generateUuid()
        )
      )
    }
    return def.groupUuId
  }

  suspend fun mapAll(): Map<String, ReminderGroup> {
    val list = reminderGroupRepository.getAll()
    val map = mutableMapOf<String, ReminderGroup>()
    for (group in list) map[group.groupUuId] = group
    return map
  }
}
