package com.elementary.tasks

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.elementary.tasks.core.di.*
import com.elementary.tasks.core.services.EventJobService
import com.evernote.android.job.JobManager
import io.fabric.sdk.android.Fabric
import timber.log.Timber

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
class ReminderApp : MultiDexApplication() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .dbModule(DbModule())
                .utilModule(UtilModule())
                .build()

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Fabric.with(this, Crashlytics(), Answers())
        JobManager.create(this).addJobCreator { EventJobService() }
    }

    companion object {
        lateinit var appComponent: AppComponent
            private set
    }
}
