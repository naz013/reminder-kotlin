package com.elementary.tasks.core.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;

import com.elementary.tasks.R;
import com.elementary.tasks.core.location.LocationTracker;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.reminder.ReminderDialogActivity;
import com.elementary.tasks.reminder.models.Place;
import com.elementary.tasks.reminder.models.Reminder;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class GeolocationService extends Service {

    private static final String TAG = "GeolocationService";
    public static final int NOTIFICATION_ID = 1245;

    private LocationTracker mTracker;
    private boolean isNotificationEnabled;
    private int stockRadius;

    private LocationTracker.Callback mLocationCallback = (lat, lon) -> {
        Location locationA = new Location("point A");
        locationA.setLatitude(lat);
        locationA.setLongitude(lon);
        checkReminders(locationA);
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTracker != null) mTracker.removeUpdates();
        stopForeground(true);
        LogUtil.d(TAG, "geo service stop");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "geo service started");
        isNotificationEnabled = Prefs.getInstance(getApplicationContext()).isDistanceNotificationEnabled();
        stockRadius = Prefs.getInstance(getApplicationContext()).getRadius();
        mTracker = new LocationTracker(getApplicationContext(), mLocationCallback);
        showDefaultNotification();
        return START_STICKY;
    }

    private void checkReminders(Location locationA) {
        for (Reminder reminder : RealmDb.getInstance().getEnabledReminders()) {
            if (Reminder.isGpsType(reminder.getType())) {
                checkDistance(locationA, reminder);
            }
        }
    }

    private void checkDistance(Location locationA, Reminder reminder) {
        if (!TextUtils.isEmpty(reminder.getEventTime())) {
            if (TimeCount.isCurrent(reminder.getEventTime())) {
                selectBranch(locationA, reminder);
            }
        } else {
            selectBranch(locationA, reminder);
        }
    }

    private void selectBranch(Location locationA, Reminder reminder) {
        if (reminder.isNotificationShown()) return;
        if (Reminder.isBase(reminder.getType(), Reminder.BY_OUT)) {
            checkOut(locationA, reminder);
        } else if (Reminder.isBase(reminder.getType(), Reminder.BY_PLACES)) {
            checkPlaces(locationA, reminder);
        } else {
            checkSimple(locationA, reminder);
        }
    }

    private void checkSimple(Location locationA, Reminder reminder) {
        Place place = reminder.getPlaces().get(0);
        Location locationB = new Location("point B");
        locationB.setLatitude(place.getLatitude());
        locationB.setLongitude(place.getLongitude());
        float distance = locationA.distanceTo(locationB);
        int roundedDistance = Math.round(distance);
        if (roundedDistance <= getRadius(place.getRadius())) {
            showReminder(reminder);
        } else {
            showNotification(roundedDistance, reminder);
        }
    }

    private void checkPlaces(Location locationA, Reminder reminder) {
        for (Place place : reminder.getPlaces()) {
            Location locationB = new Location("point B");
            locationB.setLatitude(place.getLatitude());
            locationB.setLongitude(place.getLongitude());
            float distance = locationA.distanceTo(locationB);
            int roundedDistance = Math.round(distance);
            if (roundedDistance <= getRadius(place.getRadius())) {
                showReminder(reminder);
                break;
            }
        }
    }

    private int getRadius(int radius) {
        if (radius == -1) radius = stockRadius;
        return radius;
    }

    private void checkOut(Location locationA, Reminder reminder) {
        Place place = reminder.getPlaces().get(0);
        Location locationB = new Location("point B");
        locationB.setLatitude(place.getLatitude());
        locationB.setLongitude(place.getLongitude());
        float distance = locationA.distanceTo(locationB);
        int roundedDistance = Math.round(distance);
        if (reminder.isLocked()) {
            if (roundedDistance > getRadius(place.getRadius())) {
                showReminder(reminder);
            } else {
                if (isNotificationEnabled) {
                    showNotification(roundedDistance, reminder);
                }
            }
        } else {
            if (roundedDistance < getRadius(place.getRadius())) {
                RealmDb.getInstance().saveReminder(reminder.setLocked(true), null);
            }
        }
    }

    private void showReminder(Reminder reminder) {
        if (reminder.isNotificationShown()) return;
        RealmDb.getInstance().saveReminder(reminder.setNotificationShown(true), null);
        getApplication().startActivity(ReminderDialogActivity.getLaunchIntent(getApplicationContext(), reminder.getUuId()));
    }

    private void showNotification(int roundedDistance, Reminder reminder) {
        if (!isNotificationEnabled) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Notifier.CHANNEL_SYSTEM);
        builder.setContentText(String.valueOf(roundedDistance));
        builder.setContentTitle(reminder.getSummary());
        builder.setContentText(String.valueOf(roundedDistance));
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_navigation_white_24dp);
        } else {
            builder.setSmallIcon(R.drawable.ic_navigation_nv_white);
        }
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void showDefaultNotification() {
        if (!isNotificationEnabled) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Notifier.CHANNEL_SYSTEM);
        if (Module.isPro()) {
            builder.setContentText(getString(R.string.app_name_pro));
        } else {
            builder.setContentText(getString(R.string.app_name));
        }

        builder.setContentTitle(getString(R.string.location_tracking_service_running));
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_navigation_white_24dp);
        } else {
            builder.setSmallIcon(R.drawable.ic_navigation_nv_white);
        }
        startForeground(NOTIFICATION_ID, builder.build());
    }
}
