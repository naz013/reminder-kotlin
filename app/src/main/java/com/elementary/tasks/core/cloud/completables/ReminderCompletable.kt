package com.elementary.tasks.core.cloud.completables

import com.elementary.tasks.core.controller.EventControlFactory
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.groups.GroupsUtil
import com.github.naz013.domain.Reminder
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository

class ReminderCompletable(
  private val reminderGroupRepository: ReminderGroupRepository,
  private val reminderRepository: ReminderRepository,
  private val eventControlFactory: EventControlFactory,
  private val groupsUtil: GroupsUtil,
  private val dateTimeManager: DateTimeManager
) : Completable<Reminder> {

  override suspend fun action(t: Reminder) {
    val groups = groupsUtil.mapAll()
    val defGroup = reminderGroupRepository.defaultGroup() ?: groups.values.first()

    if (!groups.containsKey(t.groupUuId)) {
      t.apply {
        this.groupTitle = defGroup.groupTitle
        this.groupUuId = defGroup.groupUuId
        this.groupColor = defGroup.groupColor
      }
    }
    if (!t.isActive || t.isRemoved) {
      t.isActive = false
    }
    if (!Reminder.isGpsType(t.type) && !dateTimeManager.isCurrent(t.eventTime)) {
      if (!Reminder.isSame(t.type, Reminder.BY_DATE_SHOP) || t.hasReminder) {
        t.isActive = false
      }
    }
    reminderRepository.save(t)
    if (t.isActive && !t.isRemoved) {
      val control = eventControlFactory.getController(t)
      if (control.canSkip()) {
        control.next()
      } else {
        control.enable()
      }
    }
  }
}
