package com.elementary.tasks

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.bumptech.glide.Glide
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.adapterModule
import com.elementary.tasks.core.utils.completableModule
import com.elementary.tasks.core.utils.converterModule
import com.elementary.tasks.core.utils.dbModule
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.elementary.tasks.core.utils.repositoryModule
import com.elementary.tasks.core.utils.storageModule
import com.elementary.tasks.core.utils.utilModule
import com.elementary.tasks.core.utils.viewModelModule
import com.elementary.tasks.core.utils.workerModule
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import timber.log.Timber

@Suppress("unused")
class ReminderApp : MultiDexApplication(), KoinComponent {

  override fun attachBaseContext(base: Context) {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }

  override fun onCreate() {
    super.onCreate()
    DynamicColors.applyToActivitiesIfAvailable(this)
    if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    val logger = object : Logger(level = Level.DEBUG) {
      override fun display(level: Level, msg: MESSAGE) {
      }
    }
    startKoin {
      logger(logger)
      androidContext(this@ReminderApp)
      workManagerFactory()
      modules(
        listOf(
          utilModule,
          dbModule(this@ReminderApp),
          repositoryModule,
          storageModule,
          completableModule,
          converterModule,
          workerModule,
          viewModelModule,
          adapterModule
        )
      )
    }

    get<Notifier>().createChannels()
    AdsProvider.init(this)
    get<RemotePrefs>().preLoad()
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
