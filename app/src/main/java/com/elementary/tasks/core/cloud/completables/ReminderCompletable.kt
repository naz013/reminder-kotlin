package com.elementary.tasks.core.cloud.completables

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.groups.GroupsUtil

class ReminderCompletable(
  private val reminderGroupDao: ReminderGroupDao,
  private val reminderDao: ReminderDao,
  private val eventControlFactory: EventControlFactory,
  private val groupsUtil: GroupsUtil,
  private val dateTimeManager: DateTimeManager
) : Completable<Reminder> {

  override suspend fun action(t: Reminder) {
    val groups = groupsUtil.mapAll()
    val defGroup = reminderGroupDao.defaultGroup() ?: groups.values.first()

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
    reminderDao.insert(t)
    if (t.isActive && !t.isRemoved) {
      val control = eventControlFactory.getController(t)
      if (control.canSkip()) {
        control.next()
      } else {
        control.start()
      }
    }
  }
}