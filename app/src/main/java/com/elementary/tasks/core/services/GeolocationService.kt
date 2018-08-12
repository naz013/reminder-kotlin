package com.elementary.tasks.core.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.reminder.preview.ReminderDialogActivity
import javax.inject.Inject

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
class GeolocationService : Service() {

    private var mTracker: LocationTracker? = null
    private var isNotificationEnabled: Boolean = false
    private var stockRadius: Int = 0
    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mTracker != null) mTracker!!.removeUpdates()
        stopForeground(true)
        LogUtil.d(TAG, "geo service stop")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        LogUtil.d(TAG, "geo service started")
        isNotificationEnabled = prefs.isDistanceNotificationEnabled
        stockRadius = prefs.radius
        mTracker = LocationTracker(applicationContext) { lat, lng ->
            val locationA = Location("point A")
            locationA.latitude = lat
            locationA.longitude = lng
            checkReminders(locationA)
        }
        showDefaultNotification()
        return Service.START_STICKY
    }

    private fun checkReminders(locationA: Location) {
        for (reminder in AppDb.getAppDatabase(applicationContext).reminderDao().getAll(true, false)) {
            if (Reminder.isGpsType(reminder.type)) {
                checkDistance(locationA, reminder)
            }
        }
    }

    private fun checkDistance(locationA: Location, reminder: Reminder) {
        if (!TextUtils.isEmpty(reminder.eventTime)) {
            if (TimeCount.isCurrent(reminder.eventTime)) {
                selectBranch(locationA, reminder)
            }
        } else {
            selectBranch(locationA, reminder)
        }
    }

    private fun selectBranch(locationA: Location, reminder: Reminder) {
        if (reminder.isNotificationShown) return
        when {
            Reminder.isBase(reminder.type, Reminder.BY_OUT) -> checkOut(locationA, reminder)
            Reminder.isBase(reminder.type, Reminder.BY_PLACES) -> checkPlaces(locationA, reminder)
            else -> checkSimple(locationA, reminder)
        }
    }

    private fun checkSimple(locationA: Location, reminder: Reminder) {
        val place = reminder.places[0]
        val locationB = Location("point B")
        locationB.latitude = place.latitude
        locationB.longitude = place.longitude
        val distance = locationA.distanceTo(locationB)
        val roundedDistance = Math.round(distance)
        if (roundedDistance <= getRadius(place.radius)) {
            showReminder(reminder)
        } else {
            showNotification(roundedDistance, reminder)
        }
    }

    private fun checkPlaces(locationA: Location, reminder: Reminder) {
        for (place in reminder.places) {
            val locationB = Location("point B")
            locationB.latitude = place.latitude
            locationB.longitude = place.longitude
            val distance = locationA.distanceTo(locationB)
            val roundedDistance = Math.round(distance)
            if (roundedDistance <= getRadius(place.radius)) {
                showReminder(reminder)
                break
            }
        }
    }

    private fun getRadius(radius: Int): Int {
        var radius = radius
        if (radius == -1) radius = stockRadius
        return radius
    }

    private fun checkOut(locationA: Location, reminder: Reminder) {
        val place = reminder.places[0]
        val locationB = Location("point B")
        locationB.latitude = place.latitude
        locationB.longitude = place.longitude
        val distance = locationA.distanceTo(locationB)
        val roundedDistance = Math.round(distance)
        if (reminder.isLocked) {
            if (roundedDistance > getRadius(place.radius)) {
                showReminder(reminder)
            } else {
                if (isNotificationEnabled) {
                    showNotification(roundedDistance, reminder)
                }
            }
        } else {
            if (roundedDistance < getRadius(place.radius)) {
                reminder.isLocked = true
                AppDb.getAppDatabase(applicationContext).reminderDao().insert(reminder)
            }
        }
    }

    private fun showReminder(reminder: Reminder) {
        if (reminder.isNotificationShown) return
        reminder.isNotificationShown = true
        AppDb.getAppDatabase(applicationContext).reminderDao().insert(reminder)
        application.startActivity(ReminderDialogActivity.getLaunchIntent(applicationContext, reminder.uuId))
    }

    private fun showNotification(roundedDistance: Int, reminder: Reminder) {
        if (!isNotificationEnabled) return
        val builder = NotificationCompat.Builder(applicationContext, Notifier.CHANNEL_SYSTEM)
        builder.setContentText(roundedDistance.toString())
        builder.setContentTitle(reminder.summary)
        builder.setContentText(roundedDistance.toString())
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_navigation_white_24dp)
        } else {
            builder.setSmallIcon(R.drawable.ic_navigation_nv_white)
        }
        startForeground(NOTIFICATION_ID, builder.build())
    }

    private fun showDefaultNotification() {
        if (!isNotificationEnabled) return
        val builder = NotificationCompat.Builder(applicationContext, Notifier.CHANNEL_SYSTEM)
        if (Module.isPro) {
            builder.setContentText(getString(R.string.app_name_pro))
        } else {
            builder.setContentText(getString(R.string.app_name))
        }

        builder.setContentTitle(getString(R.string.location_tracking_service_running))
        if (Module.isLollipop) {
            builder.setSmallIcon(R.drawable.ic_navigation_white_24dp)
        } else {
            builder.setSmallIcon(R.drawable.ic_navigation_nv_white)
        }
        startForeground(NOTIFICATION_ID, builder.build())
    }

    companion object {

        private const val TAG = "GeolocationService"
        const val NOTIFICATION_ID = 1245
    }
}
