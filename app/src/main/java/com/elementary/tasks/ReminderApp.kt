package com.elementary.tasks

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.elementary.tasks.birthdays.birthdaysModule
import com.elementary.tasks.core.appwidgets.widgetModule
import com.elementary.tasks.core.data.adapter.adapterModule
import com.elementary.tasks.core.data.factory.dataFactory
import com.elementary.tasks.core.data.repository.repositoryModule
import com.elementary.tasks.core.os.osModule
import com.elementary.tasks.core.services.action.actionModule
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.completableModule
import com.elementary.tasks.core.utils.converterModule
import com.elementary.tasks.core.utils.dataFlowRepositoryModule
import com.elementary.tasks.core.utils.dbModule
import com.elementary.tasks.core.utils.newUtilsModule
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.elementary.tasks.core.utils.storageModule
import com.elementary.tasks.core.utils.ui.uiUtilsModule
import com.elementary.tasks.core.utils.utilModule
import com.elementary.tasks.core.utils.viewModelModule
import com.elementary.tasks.core.utils.workerModule
import com.elementary.tasks.dayview.dayViewModule
import com.elementary.tasks.globalsearch.searchModule
import com.elementary.tasks.monthview.monthViewModule
import com.elementary.tasks.reminder.reminderModule
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
          dataFlowRepositoryModule,
          storageModule,
          completableModule,
          converterModule,
          workerModule,
          viewModelModule,
          adapterModule,
          actionModule,
          repositoryModule,
          dataFactory,
          uiUtilsModule,
          reminderModule,
          widgetModule,
          osModule,
          newUtilsModule,
          birthdaysModule,
          dayViewModule,
          monthViewModule,
          searchModule
        )
      )
    }

    get<Notifier>().createChannels()
    AdsProvider.init(this)
    get<RemotePrefs>().preLoad()
  }
}
