package com.elementary.tasks

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.elementary.tasks.birthdays.birthdaysModule
import com.elementary.tasks.calendar.calendarModule
import com.elementary.tasks.core.cloud.cloudModule
import com.elementary.tasks.core.data.adapter.adapterModule
import com.elementary.tasks.core.os.osModule
import com.elementary.tasks.core.services.action.actionModule
import com.elementary.tasks.core.services.servicesModule
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.newUtilsModule
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.elementary.tasks.core.utils.storageModule
import com.elementary.tasks.core.utils.ui.uiUtilsModule
import com.elementary.tasks.core.utils.utilModule
import com.elementary.tasks.core.utils.viewModelModule
import com.elementary.tasks.core.utils.workerModule
import com.elementary.tasks.core.work.workModule
import com.elementary.tasks.globalsearch.searchModule
import com.elementary.tasks.googletasks.googleTaskModule
import com.elementary.tasks.groups.reminderGroupModule
import com.elementary.tasks.home.homeModule
import com.elementary.tasks.navigation.NavigationConsumer
import com.elementary.tasks.navigation.NavigationDispatcherFactory
import com.elementary.tasks.navigation.NavigationObservable
import com.elementary.tasks.navigation.navigationModule
import com.elementary.tasks.notes.noteModule
import com.elementary.tasks.places.placeKoinModule
import com.elementary.tasks.reminder.reminderModule
import com.github.naz013.appwidgets.appWidgetsModule
import com.github.naz013.cloudapi.cloudApiModule
import com.github.naz013.common.platformCommonModule
import com.github.naz013.feature.common.featureCommonModule
import com.github.naz013.icalendar.iCalendarModule
import com.github.naz013.logging.initLogging
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DataDestination
import com.github.naz013.navigation.Destination
import com.github.naz013.navigation.navigationApiModule
import com.github.naz013.repository.repositoryModule
import com.github.naz013.reviews.ReviewSdk
import com.github.naz013.reviews.config.SecondaryFirebaseConfig
import com.github.naz013.reviews.reviewsKoinModule
import com.github.naz013.sync.syncApiModule
import com.github.naz013.ui.common.uiCommonModule
import com.github.naz013.usecase.birthdays.birthdaysUseCaseModule
import com.github.naz013.usecase.googletasks.googleTasksUseCaseModule
import com.github.naz013.usecase.notes.notesUseCaseModule
import com.github.naz013.usecase.reminders.remindersUseCaseModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

@Suppress("unused")
class ReminderApp : MultiDexApplication(), KoinComponent {

  private val navigationConsumer = object : NavigationConsumer {
    override fun consume(destination: Destination) {
      if (destination is ActivityDestination || destination is DataDestination) {
        get<NavigationDispatcherFactory>().create(destination).dispatch(destination)
      } else {
        com.github.naz013.logging.Logger.i("App", "Unknown destination: $destination")
      }
    }
  }

  override fun attachBaseContext(base: Context) {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }

  override fun onCreate() {
    super.onCreate()
    initLogging(
      isDebug = BuildConfig.DEBUG
    )
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
          featureCommonModule,
          storageModule,
          workerModule,
          viewModelModule,
          adapterModule,
          actionModule,
          uiUtilsModule,
          reminderModule,
          osModule,
          newUtilsModule,
          birthdaysModule,
          calendarModule,
          searchModule,
          homeModule,
          googleTaskModule,
          workModule,
          noteModule,
          servicesModule,
          repositoryModule,
          cloudApiModule,
          platformCommonModule,
          navigationModule,
          uiCommonModule,
          appWidgetsModule,
          googleTasksUseCaseModule,
          birthdaysUseCaseModule,
          remindersUseCaseModule,
          notesUseCaseModule,
          iCalendarModule,
          navigationApiModule,
          cloudModule,
          syncApiModule,
          reminderGroupModule,
          placeKoinModule,
          reviewsKoinModule
        )
      )
    }

    val config = SecondaryFirebaseConfig(
      projectId = BuildConfig.REVIEWS_PROJECT_ID,
      applicationId = BuildConfig.REVIEWS_APP_ID,
      apiKey = BuildConfig.REVIEWS_API_KEY,
      storageBucket = BuildConfig.REVIEWS_STORAGE_BUCKET
    )

    ReviewSdk.initialize(this, config, true).fold(
      onSuccess = {
        com.github.naz013.logging.Logger.i("App", "✅ Reviews Firebase initialized")
      },
      onFailure = { error ->
        com.github.naz013.logging.Logger.e("App", "❌ Reviews init failed", error)
      }
    )

    get<NavigationObservable>().subscribeGlobal(navigationConsumer)

    get<Notifier>().createChannels()
    AdsProvider.init(this)
    get<RemotePrefs>().preLoad()

    registerActivityLifecycleCallbacks(ActivityObserver(get()))
  }
}
