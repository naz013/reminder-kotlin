package com.elementary.tasks.core.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import com.elementary.tasks.core.utils.Prefs;

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

public class LocationTracker implements LocationListener {

    private Context mContext;
    private LocationManager mLocationManager;
    private Callback mCallback;

    public LocationTracker(Context context, Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
        updateListener();
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if (mCallback != null) {
            mCallback.onChange(latitude, longitude);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        updateListener();
    }

    @Override
    public void onProviderEnabled(String provider) {
        updateListener();
    }

    @Override
    public void onProviderDisabled(String provider) {
        updateListener();
    }

    public void removeUpdates() {
        mLocationManager.removeUpdates(this);
    }

    private void updateListener() {
        if (mContext == null) {
            return;
        }
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        long time = (Prefs.getInstance(mContext).getTrackTime() * 1000) * 2;
        int distance = Prefs.getInstance(mContext).getTrackDistance() * 2;
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, this);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, this);
        }
    }

    public interface Callback {
        void onChange(double lat, double lon);
    }
}
