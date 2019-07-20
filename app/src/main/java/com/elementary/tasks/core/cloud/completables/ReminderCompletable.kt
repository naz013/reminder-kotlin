package com.elementary.tasks.core.cloud.completables

import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.groups.GroupsUtil
import org.koin.core.KoinComponent
import org.koin.core.inject

class ReminderCompletable : Completable<Reminder>, KoinComponent {

    private val appDb: AppDb by inject()

    override suspend fun action(t: Reminder) {
        val groups = GroupsUtil.mapAll(appDb)
        val defGroup = appDb.reminderGroupDao().defaultGroup() ?: groups.values.first()

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
        if (!Reminder.isGpsType(t.type) && !TimeCount.isCurrent(t.eventTime)) {
            if (!Reminder.isSame(t.type, Reminder.BY_DATE_SHOP) || t.hasReminder) {
                t.isActive = false
            }
        }
        appDb.reminderDao().insert(t)
        if (t.isActive && !t.isRemoved) {
            val control = EventControlFactory.getController(t)
            if (control.canSkip()) {
                control.next()
            } else {
                control.start()
            }
        }
    }
}