package com.elementary.tasks.core.cloud.completables

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.groups.GroupsUtil

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class ReminderCompletable(
  private val appDb: AppDb,
  private val eventControlFactory: EventControlFactory
) : Completable<Reminder> {

  override suspend fun action(reminder: Reminder) {
    val groups = GroupsUtil.mapAll(appDb)
    val defGroup = appDb.reminderGroupDao().defaultGroup() ?: groups.values.first()

    if (!groups.containsKey(reminder.groupUuId)) {
      reminder.apply {
        this.groupTitle = defGroup.groupTitle
        this.groupUuId = defGroup.groupUuId
        this.groupColor = defGroup.groupColor
      }
    }
    if (!reminder.isActive || reminder.isRemoved) {
      reminder.isActive = false
    }
    if (!Reminder.isGpsType(reminder.type) && !TimeCount.isCurrent(reminder.eventTime)) {
      if (!Reminder.isSame(reminder.type, Reminder.BY_DATE_SHOP) || reminder.hasReminder) {
        reminder.isActive = false
      }
    }
    appDb.reminderDao().insert(reminder)
    if (reminder.isActive && !reminder.isRemoved) {
      val control = eventControlFactory.getController(reminder)
      if (control.canSkip()) {
        control.next()
      } else {
        control.start()
      }
    }
  }
}