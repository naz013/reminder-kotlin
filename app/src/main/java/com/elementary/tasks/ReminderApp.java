package com.elementary.tasks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.FontRequestEmojiCompatConfig;
import android.support.v4.provider.FontRequest;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.elementary.tasks.core.services.EventJobService;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.evernote.android.job.JobManager;

import io.fabric.sdk.android.Fabric;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import timber.log.Timber;

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

    private static final long DB_VERSION = 4;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
        final FontRequest fontRequest = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs);
        EmojiCompat.Config config = new FontRequestEmojiCompatConfig(getApplicationContext(), fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(new EmojiCompat.InitCallback() {
                    @Override
                    public void onInitialized() {
                        LogUtil.i(TAG, "EmojiCompat initialized");
                    }

                    @Override
                    public void onFailed(@Nullable Throwable throwable) {
                        LogUtil.d(TAG, "EmojiCompat initialization failed" + throwable);
                    }
                });
        EmojiCompat.init(config);
        Notifier.createChannels(this);
        Fabric.with(this, new Crashlytics(), new Answers());
        Prefs.getInstance(this);
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(DB_VERSION)
                .name(BuildConfig.IS_PRO ? NAME_DB_PRO : NAME_DB)
                .migration(new Migration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        JobManager.create(this).addJobCreator(tag -> new EventJobService());
    }

    private static class Migration implements RealmMigration {
        @Override
        public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
            LogUtil.d(TAG, "migrate: " + oldVersion + ", " + newVersion + ", " + realm.getSchema());
            RealmSchema schema = realm.getSchema();
            if (oldVersion == 1) {
                RealmObjectSchema model = schema.get("RealmReminder");
                if (model != null) {
                    model.addField("duration", long.class).transform(obj -> obj.setLong("duration", 0));
                    model.addField("monthOfYear", int.class).transform(obj -> obj.setLong("monthOfYear", 0));
                    model.addField("remindBefore", long.class).transform(obj -> obj.setLong("remindBefore", 0));
                }
            } else if (oldVersion == 2) {
                RealmObjectSchema model = schema.get("RealmReminder");
                if (model != null) {
                    model.addField("monthOfYear", int.class).transform(obj -> obj.setLong("monthOfYear", 0));
                    model.addField("remindBefore", long.class).transform(obj -> obj.setLong("remindBefore", 0));
                }
            } else if (oldVersion == 3) {
                RealmObjectSchema model = schema.get("RealmReminder");
                if (model != null) {
                    model.addField("remindBefore", long.class).transform(obj -> obj.setLong("remindBefore", 0));
                }
            }
        }
    }
}
