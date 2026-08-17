package com.elementary.tasks

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.elementary.tasks.appfunctions.AppFunctionsInitializer
import com.elementary.tasks.core.cloud.cloudModule
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
import com.elementary.tasks.module.libModule
import com.elementary.tasks.module.platform.InstallReferrerReader
import com.elementary.tasks.navigation.NavigationConsumer
import com.elementary.tasks.navigation.NavigationDispatcher
import com.elementary.tasks.navigation.NavigationObservable
import com.elementary.tasks.navigation.navigationModule
import com.elementary.tasks.places.placeKoinModule
import com.elementary.tasks.reminder.reminderModule
import com.elementary.tasks.telephony.intentModule
import com.github.naz013.appwidgets.appWidgetsModule
import com.github.naz013.cloudapi.cloudApiModule
import com.github.naz013.common.platformCommonModule
import com.github.naz013.datecalc.dateTimeCalculationsModule
import com.github.naz013.feature.agenda.featureAgendaModule
import com.github.naz013.feature.birthday.featureBirthdayModule
import com.github.naz013.feature.calendar.featureCalendarModule
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.featureCommonModule
import com.github.naz013.feature.googletask.featureGoogleTaskModule
import com.github.naz013.feature.home.featureHomeModule
import com.github.naz013.feature.note.featureNoteModule
import com.github.naz013.feature.reminder.featureReminderModule
import com.github.naz013.feature.settings.featureSettingsModule
import com.github.naz013.feature.workflow.workflowModule
import com.github.naz013.files.fileModule
import com.github.naz013.group.groupModule
import com.github.naz013.holidays.holidaysModule
import com.github.naz013.icalendar.iCalendarModule
import com.github.naz013.insights.insightsModule
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.legalModule
import com.github.naz013.localbackup.localBackupModule
import com.github.naz013.logging.initLogging
import com.github.naz013.logic.birthday.logicBirthdayModule
import com.github.naz013.logic.googletask.logicGoogleTaskModule
import com.github.naz013.logic.reminder.logicReminderModule
import com.github.naz013.logic.schedule.logicScheduleModule
import com.github.naz013.logic.tag.logicTagModule
import com.github.naz013.logic.workflow.logicWorkflowModule
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
import com.github.naz013.ui.agenda.uiAgendaModule
import com.github.naz013.ui.birthday.uiBirthdayModule
import com.github.naz013.ui.common.locale.Language
import com.github.naz013.ui.common.uiCommonModule
import com.github.naz013.ui.googletask.uiGoogleTaskModule
import com.github.naz013.ui.group.uiGroupModule
import com.github.naz013.ui.map.uiMapModule
import com.github.naz013.ui.note.uiNoteModule
import com.github.naz013.ui.reminder.uiReminderModule
import com.github.naz013.ui.tag.uiTagModule
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
      try {
        workManagerFactory()
      } catch (t: Throwable) {
        // Some OEM Android 14 builds report SDK_INT 34 but ship a framework.jar missing
        // JobScheduler.forNamespace(), which WorkManager calls unconditionally on init.
        // Swallow it so the rest of DI still wires up; WorkScheduler falls back to no-ops.
        com.github.naz013.logging.Logger.e("App", "Failed to initialize WorkManager", t)
      }
      modules(
        listOf(
          utilModule,
          featureCommonModule,
          featureNoteModule,
          uiNoteModule,
          storageModule,
          viewModelModule,
          actionModule,
          uiUtilsModule,
          reminderModule,
          osModule,
          newUtilsModule,
          featureBirthdayModule,
          uiBirthdayModule,
          logicBirthdayModule,
          featureCalendarModule,
          featureAgendaModule,
          featureHomeModule,
          featureGoogleTaskModule,
          logicGoogleTaskModule,
          servicesModule,
          repositoryModule,
          cloudApiModule,
          platformCommonModule,
          navigationModule,
          uiCommonModule,
          appWidgetsModule,
          iCalendarModule,
          navigationApiModule,
          cloudModule,
          syncApiModule,
          groupModule,
          placeKoinModule,
          uiMapModule,
          reviewsKoinModule,
          featureSettingsModule,
          legalModule,
          workModule,
          holidaysModule,
          dateTimeCalculationsModule,
          libModule,
          intentModule,
          workflowModule,
          logicWorkflowModule,
          fileModule,
          tagsModule,
          insightsModule,
          localBackupModule,
          uiGoogleTaskModule,
          logicScheduleModule,
          logicReminderModule,
          featureReminderModule,
          uiTagModule,
          logicTagModule,
          uiGroupModule,
          uiAgendaModule,
          uiReminderModule
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
    get<InstallReferrerReader>().readOnce()
    get<RemotePrefs>().preLoad()
    CoroutineScope(get<DispatcherProvider>().io()).launch { get<LegalDocumentRepository>().refresh() }

    registerActivityLifecycleCallbacks(ActivityObserver(get()))
  }
}
