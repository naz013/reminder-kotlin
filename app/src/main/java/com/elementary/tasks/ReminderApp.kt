package com.elementary.tasks

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.elementary.tasks.core.services.EventJobService
import com.elementary.tasks.core.utils.components
import com.evernote.android.job.JobManager
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class ReminderApp : MultiDexApplication() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
//        val prefs = AndroidExcludedRefs.createAppDefaults()
//                .instanceField("android.widget.TextView$3", "this$0")
//                .build()
//        val config = LeakCanary.Config(
//                LeakCanary.config.dumpHeap,
//                prefs,
//                LeakCanary.config.reachabilityInspectorClasses,
//                LeakCanary.config.labelers,
//                LeakCanary.config.computeRetainedHeapSize
//        )
//        LeakCanary.config = config
        Timber.plant(Timber.DebugTree())
        Fabric.with(this, Crashlytics())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        startKoin(this, components())
        JobManager.create(this).addJobCreator { EventJobService() }
    }
}
