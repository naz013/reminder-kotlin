package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.elementary.tasks.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2017 Nazar Suhovich
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

public class RemotePrefs {

    private static final String SALE_STARTED = "sale_started";
    private static final String SALE_VALUE = "sale_save_value";
    private static final String SALE_EXPIRY_DATE = "sale_until_time_utc";

    private static final String VERSION_CODE = "version_code";
    private static final String VERSION_NAME = "version_name";

    private static final String TAG = "RemotePrefs";

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private static RemotePrefs instance;

    private List<SaleObserver> mObservers = new ArrayList<>();
    private List<UpdateObserver> mUpdateObservers = new ArrayList<>();
    private PackageManager pm;
    private String packageName;

    public static RemotePrefs getInstance(Context context) {
        if (instance == null) {
            synchronized (RemotePrefs.class) {
                if (instance == null) {
                    instance = new RemotePrefs(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private RemotePrefs(Context context) {
        this.pm = context.getPackageManager();
        this.packageName = context.getPackageName();
        this.mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        this.mFirebaseRemoteConfig.setConfigSettings(configSettings);
        this.mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        fetchConfig();
    }

    private void fetchConfig() {
        mFirebaseRemoteConfig.fetch(3600).addOnCompleteListener(task -> {
            LogUtil.d(TAG, "fetchConfig: " + task.isSuccessful());
            if (task.isSuccessful()) {
                mFirebaseRemoteConfig.activateFetched();
            }
            displayVersionMessage();
            if (!Module.isPro()) displaySaleMessage();
        });
    }

    public void addUpdateObserver(UpdateObserver observer) {
        if (!mUpdateObservers.contains(observer)) {
            mUpdateObservers.add(observer);
        }
        fetchConfig();
    }

    public void removeUpdateObserver(UpdateObserver observer) {
        if (mUpdateObservers.contains(observer)) {
            mUpdateObservers.remove(observer);
        }
    }

    public void addSaleObserver(SaleObserver observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
        fetchConfig();
    }

    public void removeSaleObserver(SaleObserver observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    private void displayVersionMessage() {
        long versionCode = mFirebaseRemoteConfig.getLong(VERSION_CODE);
        try {
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);
            int verCode = pInfo.versionCode;
            if (versionCode > verCode) {
                String version = mFirebaseRemoteConfig.getString(VERSION_NAME);
                for (UpdateObserver observer : mUpdateObservers) {
                    observer.onUpdate(version);
                }
                return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        notifyNoUpdate();
    }

    private void notifyNoUpdate() {
        for (UpdateObserver observer : mUpdateObservers) {
            observer.noUpdate();
        }
    }

    private void displaySaleMessage() {
        boolean isSale = mFirebaseRemoteConfig.getBoolean(SALE_STARTED);
        if (isSale) {
            String expiry = mFirebaseRemoteConfig.getString(SALE_EXPIRY_DATE);
            String discount = mFirebaseRemoteConfig.getString(SALE_VALUE);
            for (SaleObserver observer : mObservers) {
                observer.onSale(discount, expiry);
            }
            return;
        }
        notifyNoSale();
    }

    private void notifyNoSale() {
        for (SaleObserver observer : mObservers) {
            observer.noSale();
        }
    }

    public interface UpdateObserver {
        void onUpdate(String version);

        void noUpdate();
    }

    public interface SaleObserver {
        void onSale(String discount, String expiryDate);

        void noSale();
    }
}
