package com.elementary.tasks.core.controller

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import timber.log.Timber

class WeeklyEvent(reminder: Reminder) : RepeatableEventManager(reminder) {

    override val isActive: Boolean
        get() = reminder.isActive

    override fun start(): Boolean {
        Timber.d("start: ${reminder.eventTime}")
        if (TimeCount.isCurrent(reminder.eventTime)) {
            reminder.isActive = true
            reminder.isRemoved = false
            super.save()
            super.enableReminder()
            super.export()
            return true
        }
        return false
    }

    override fun skip(): Boolean {
        if (canSkip()) {
            val time = TimeCount.getNextWeekdayTime(reminder, TimeUtil.getDateTimeFromGmt(reminder.eventTime) + 1000L)
            reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
            start()
            return true
        }
        return false
    }

    override fun next(): Boolean {
        reminder.delay = 0
        return if (canSkip()) {
            val time = calculateTime(false)
            reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
            reminder.eventCount = reminder.eventCount + 1
            start()
        } else {
            stop()
        }
    }

    override fun onOff(): Boolean {
        return if (isActive) {
            stop()
        } else {
            if (!TimeCount.isCurrent(reminder.eventTime)) {
                val time = TimeCount.getNextWeekdayTime(reminder, TimeUtil.getDateTimeFromGmt(reminder.eventTime) + 1000L)
                reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
                reminder.startTime = TimeUtil.getGmtFromDateTime(time)
            }
            reminder.eventCount = 0
            start()
        }
    }

    override fun canSkip(): Boolean {
        return !reminder.isLimited() || !reminder.isLimitExceed()
    }

    override fun setDelay(delay: Int) {
        if (delay == 0) {
            next()
            return
        }
        reminder.delay = delay
        super.save()
        super.setDelay(delay)
    }

    override fun calculateTime(isNew: Boolean): Long {
        return TimeCount.getNextWeekdayTime(reminder)
    }
}