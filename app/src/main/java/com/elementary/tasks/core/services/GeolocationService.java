package com.elementary.tasks.core.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.elementary.tasks.R;
import com.elementary.tasks.core.location.LocationTracker;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
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
        mTracker.removeUpdates();
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
                RealmDb.getInstance().saveObject(reminder.setLocked(true));
            }
        }
    }

    private void showReminder(Reminder reminder) {
        if (reminder.isNotificationShown()) return;
        RealmDb.getInstance().saveObject(reminder.setNotificationShown(true));
        Intent resultIntent = new Intent(getApplicationContext(), ReminderDialogActivity.class);
        resultIntent.putExtra(Constants.INTENT_ID, reminder.getUuId());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        getApplication().startActivity(resultIntent);
    }

    private void showNotification(int roundedDistance, Reminder reminder) {
        if (!isNotificationEnabled) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentText(String.valueOf(roundedDistance));
        builder.setContentTitle(reminder.getSummary());
        builder.setContentText(String.valueOf(roundedDistance));
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_navigation_white_24dp);
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher);
        }
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(reminder.getUniqueId(), builder.build());
    }
}
