package com.elementary.tasks

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.bumptech.glide.Glide
import com.elementary.tasks.core.services.EventJobService
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.utilModule
import com.evernote.android.job.JobManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import timber.log.Timber

@Suppress("unused")
class ReminderApp : MultiDexApplication() {

  override fun attachBaseContext(base: Context) {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    val logger = object : Logger(level = Level.DEBUG) {
      override fun log(level: Level, msg: MESSAGE) {
      }
    }
    startKoin {
      logger(logger)
      androidContext(this@ReminderApp)
      modules(listOf(
        utilModule
      ))
    }
    Notifier.createChannels(this)
    JobManager.create(this).addJobCreator { EventJobService() }
    AdsProvider.init(this)
  }

  override fun onTrimMemory(level: Int) {
    if (level == TRIM_MEMORY_UI_HIDDEN) {
      Glide.get(this).clearMemory()
    }
    Glide.get(this).trimMemory(level)
    super.onTrimMemory(level)
  }

  override fun onLowMemory() {
    Glide.get(this).clearMemory()
    super.onLowMemory()
  }
}
