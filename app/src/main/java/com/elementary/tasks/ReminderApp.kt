package com.elementary.tasks

import android.content.Context
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.elementary.tasks.core.services.EventJobService
import com.elementary.tasks.core.utils.components
import com.evernote.android.job.JobManager
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.android.startKoin
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
        Timber.plant(Timber.DebugTree())
        Fabric.with(this, Crashlytics())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        startKoin(this, components())
        JobManager.create(this).addJobCreator { EventJobService() }

        val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)
        val config = FontRequestEmojiCompatConfig(applicationContext, fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        Timber.d("onInitialized: EmojiCompat initialized")
                    }

                    override fun onFailed(@Nullable throwable: Throwable?) {
                        Timber.d("onFailed: ")
                    }
                })
        EmojiCompat.init(config)
    }
}
