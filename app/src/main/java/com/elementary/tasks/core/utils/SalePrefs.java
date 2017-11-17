package com.elementary.tasks.core.utils;

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

public class SalePrefs {

    private static final String SALE_STARTED = "sale_started";
    private static final String SALE_VALUE = "sale_save_value";
    private static final String SALE_EXPIRY_DATE = "sale_until_time_utc";
    private static final String TAG = "SalePrefs";

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private static SalePrefs instance;

    private List<SaleObserver> mObservers = new ArrayList<>();

    public static SalePrefs getInstance() {
        if (instance == null) {
            synchronized (SalePrefs.class) {
                if (instance == null) {
                    instance = new SalePrefs();
                }
            }
        }
        return instance;
    }

    private SalePrefs() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        fetchConfig();
    }

    private void fetchConfig() {
        mFirebaseRemoteConfig.fetch(3600).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mFirebaseRemoteConfig.activateFetched();
            }
            displaySaleMessage();
        });
    }

    public void addObserver(SaleObserver observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
        fetchConfig();
    }

    public void removeObserver(SaleObserver observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
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

    public interface SaleObserver {
        void onSale(String discount, String expiryDate);

        void noSale();
    }
}
