package com.elementary.tasks.core.controller

import android.text.TextUtils
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.EventJobService
import com.elementary.tasks.core.services.GeolocationService
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TimeCount

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class LocationEvent(reminder: Reminder) : EventManager(reminder) {

    override val isActive: Boolean
        get() = reminder.isActive

    override val isRepeatable: Boolean
        get() = false

    override fun start(): Boolean {
        return if (Module.hasLocation(context)) {
            reminder.isActive = true
            reminder.isRemoved = false
            super.save()
            if (EventJobService.enablePositionDelay(context, reminder.uuId)) {
                true
            } else {
                SuperUtil.startGpsTracking(context)
                true
            }
        } else {
            stop()
            remove()
            false
        }
    }

    override fun stop(): Boolean {
        EventJobService.cancelReminder(reminder.uuId)
        reminder.isActive = false
        if (prefs.moveCompleted) {
            reminder.isRemoved = true
        }
        super.save()
        notifier.hideNotification(reminder.uniqueId)
        stopTracking(false)
        return true
    }

    private fun stopTracking(isPaused: Boolean) {
        val list = db.reminderDao().getAllTypes(true, false, Reminder.gpsTypes())
        if (list.isEmpty()) {
            SuperUtil.stopService(context, GeolocationService::class.java)
        }
        var hasActive = false
        for (item in list) {
            if (isPaused) {
                if (item.uniqueId == reminder.uniqueId) {
                    continue
                }
                if (TextUtils.isEmpty(item.eventTime) || !TimeCount.isCurrent(item.eventTime)) {
                    if (!item.isNotificationShown) {
                        hasActive = true
                        break
                    }
                } else {
                    if (!item.isNotificationShown) {
                        hasActive = true
                        break
                    }
                }
            } else {
                if (!item.isNotificationShown) {
                    hasActive = true
                    break
                }
            }
        }
        if (!hasActive) {
            SuperUtil.stopService(context, GeolocationService::class.java)
        }
    }

    override fun pause(): Boolean {
        EventJobService.cancelReminder(reminder.uuId)
        stopTracking(true)
        return true
    }

    override fun skip(): Boolean {
        return false
    }

    override fun resume(): Boolean {
        if (reminder.isActive) {
            val b = EventJobService.enablePositionDelay(context, reminder.uuId)
            if (!b) SuperUtil.startGpsTracking(context)
        }
        return true
    }

    override fun next(): Boolean {
        return stop()
    }

    override fun onOff(): Boolean {
        return if (isActive) {
            stop()
        } else {
            reminder.isLocked = false
            reminder.isNotificationShown = false
            super.save()
            start()
        }
    }

    override fun canSkip(): Boolean {
        return false
    }

    override fun setDelay(delay: Int) {

    }

    override fun calculateTime(isNew: Boolean): Long {
        return TimeCount.generateDateTime(reminder.eventTime, reminder.repeatInterval)
    }
}
