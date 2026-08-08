package com.elementary.tasks

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.elementary.tasks.appfunctions.AppFunctionsInitializer
import com.elementary.tasks.birthdays.birthdaysModule
import com.elementary.tasks.calendar.calendarModule
import com.elementary.tasks.core.cloud.cloudModule
import com.elementary.tasks.core.data.adapter.adapterModule
import com.elementary.tasks.core.os.osModule
import com.elementary.tasks.core.services.action.actionModule
import com.elementary.tasks.core.services.servicesModule
import com.elementary.tasks.core.utils.Notifier
import com.elementary.tasks.core.utils.newUtilsModule
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.elementary.tasks.core.utils.storageModule
import com.elementary.tasks.core.utils.ui.uiUtilsModule
import com.elementary.tasks.core.utils.utilModule
import com.elementary.tasks.core.utils.viewModelModule
import com.elementary.tasks.groups.groupModule
import com.elementary.tasks.home.homeModule
import com.elementary.tasks.module.libModule
import com.elementary.tasks.navigation.NavigationConsumer
import com.elementary.tasks.navigation.NavigationDispatcher
import com.elementary.tasks.navigation.NavigationObservable
import com.elementary.tasks.navigation.navigationModule
import com.elementary.tasks.notes.noteModule
import com.elementary.tasks.places.placeKoinModule
import com.elementary.tasks.reminder.reminderModule
import com.elementary.tasks.settings.export.syncSettingsModule
import com.elementary.tasks.settings.settingsModule
import com.elementary.tasks.simplemap.simpleMapKoinModule
import com.elementary.tasks.telephony.intentModule
import com.elementary.tasks.workflow.workflowModule
import com.github.naz013.appwidgets.appWidgetsModule
import com.github.naz013.cloudapi.cloudApiModule
import com.github.naz013.common.platformCommonModule
import com.github.naz013.datecalc.dateTimeCalculationsModule
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.featureCommonModule
import com.github.naz013.feature.googletask.featureGoogleTaskModule
import com.github.naz013.feature.note.featureNoteModule
import com.github.naz013.files.fileModule
import com.github.naz013.icalendar.iCalendarModule
import com.github.naz013.insights.insightsModule
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.legalModule
import com.github.naz013.localbackup.localBackupModule
import com.github.naz013.logging.initLogging
import com.github.naz013.logic.reminder.logicReminderModule
import com.github.naz013.logic.schedule.logicScheduleModule
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DataDestination
import com.github.naz013.navigation.Destination
import com.github.naz013.navigation.navigationApiModule
import com.github.naz013.platform.SystemInfo
import com.github.naz013.repository.repositoryModule
import com.github.naz013.reviews.ReviewSdk
import com.github.naz013.reviews.config.SecondaryFirebaseConfig
import com.github.naz013.reviews.reviewsKoinModule
import com.github.naz013.sync.syncApiModule
import com.github.naz013.tags.tagsModule
import com.github.naz013.ui.common.locale.Language
import com.github.naz013.ui.common.uiCommonModule
import com.github.naz013.ui.googletask.uiGoogleTaskModule
import com.github.naz013.usecase.birthdays.birthdaysUseCaseModule
import com.github.naz013.usecase.googletasks.googleTasksUseCaseModule
import com.github.naz013.usecase.notes.notesUseCaseModule
import com.github.naz013.usecase.reminders.remindersUseCaseModule
import com.github.naz013.work.workModule
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

@Suppress("unused")
class ReminderApp :
  MultiDexApplication(),
  KoinComponent {
  private val navigationConsumer =
    object : NavigationConsumer {
      override fun consume(destination: Destination) {
        if (destination is ActivityDestination || destination is DataDestination) {
          get<NavigationDispatcher>().dispatch(destination)
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
    // Initialize PDFBox resource loader required for PDF text extraction.
    PDFBoxResourceLoader.init(applicationContext)
    initLogging(
      isDebug = BuildConfig.DEBUG,
    )
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    val logger =
      object : Logger(level = Level.DEBUG) {
        override fun display(
          level: Level,
          msg: MESSAGE,
        ) {
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
          featureNoteModule,
          storageModule,
          viewModelModule,
          adapterModule,
          actionModule,
          uiUtilsModule,
          reminderModule,
          osModule,
          newUtilsModule,
          birthdaysModule,
          calendarModule,
          homeModule,
          featureGoogleTaskModule,
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
          groupModule,
          placeKoinModule,
          simpleMapKoinModule,
          reviewsKoinModule,
          syncSettingsModule,
          settingsModule,
          legalModule,
          workModule,
          dateTimeCalculationsModule,
          libModule,
          intentModule,
          workflowModule,
          fileModule,
          tagsModule,
          insightsModule,
          localBackupModule,
          uiGoogleTaskModule,
          logicScheduleModule,
          logicReminderModule,
        ),
      )
    }

    AppFunctionsInitializer.init()

    val config =
      SecondaryFirebaseConfig(
        projectId = BuildConfig.REVIEWS_PROJECT_ID,
        applicationId = BuildConfig.REVIEWS_APP_ID,
        apiKey = BuildConfig.REVIEWS_API_KEY,
        storageBucket = BuildConfig.REVIEWS_STORAGE_BUCKET,
      )

    ReviewSdk.initialize(this, config, true).fold(
      onSuccess = {
        com.github.naz013.logging.Logger
          .i("App", "✅ Reviews Firebase initialized")
      },
      onFailure = { error ->
        com.github.naz013.logging.Logger
          .e("App", "❌ Reviews init failed", error)
      },
    )

    // Migration continuity: AppCompatDelegate's own per-app language storage doesn't know about
    // a locale the user picked under the old attachBaseContext-based mechanism until we tell it
    // once - after that it persists this itself and this call is just a harmless no-op resync.
    AppCompatDelegate.setApplicationLocales(Language.getLocaleList(get<Prefs>().appLanguage))

    get<NavigationObservable>().subscribeGlobal(navigationConsumer)

    get<Notifier>().createChannels()
    AdsProvider.init(this, get<SystemInfo>())
    get<RemotePrefs>().preLoad()
    CoroutineScope(get<DispatcherProvider>().io()).launch { get<LegalDocumentRepository>().refresh() }

    registerActivityLifecycleCallbacks(ActivityObserver(get()))
  }
}
