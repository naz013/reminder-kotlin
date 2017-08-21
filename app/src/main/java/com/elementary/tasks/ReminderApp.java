package com.elementary.tasks;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;

import io.fabric.sdk.android.Fabric;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

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

public class ReminderApp extends MultiDexApplication {

    private static final String TAG = "ReminderApp";

    private static final String NAME_DB = "reminder_db";
    private static final String NAME_DB_PRO = "reminder_db_pro";

    private static final long DB_VERSION = 2;

    private Tracker mTracker;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notifier.createChannels(this);
        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());
        Prefs.getInstance(this);
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(DB_VERSION)
                .name(BuildConfig.IS_PRO ? NAME_DB_PRO : NAME_DB)
                .migration(new Migration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        new Instabug.Builder(this, "4a9d4ec9b3810d5a7cd7e9d474d5d3cc")
                .setInvocationEvent(InstabugInvocationEvent.SHAKE)
                .build();
    }

    public synchronized Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    private static class Migration implements RealmMigration {
        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            LogUtil.d(TAG, "migrate: " + oldVersion + ", " + newVersion + ", " + realm.getSchema());
            if (oldVersion == 1) {
                RealmSchema schema = realm.getSchema();
                RealmObjectSchema model = schema.get("RealmReminder");
                model.addField("duration", long.class)
                        .transform(obj -> obj.setLong("duration", 0));
            }
        }
    }
}
