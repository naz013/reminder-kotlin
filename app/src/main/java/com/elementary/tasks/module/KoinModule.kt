package com.elementary.tasks.module

import com.elementary.tasks.module.analytics.AnalyticsStateProviderImpl
import com.elementary.tasks.module.appwidgets.AppWidgetPreferencesImpl
import com.elementary.tasks.module.appwidgets.NoteWidgetPreferencesImpl
import com.elementary.tasks.module.featuregoogletask.GoogleTasksPreferencesImpl
import com.elementary.tasks.module.featuresettings.CalendarSettingsPreferencesImpl
import com.elementary.tasks.module.featuresettings.CloudBackupSettingsPreferencesImpl
import com.elementary.tasks.module.featuresettings.DeveloperSettingsPreferencesImpl
import com.elementary.tasks.module.featuresettings.GeneralSettingsPreferencesImpl
import com.elementary.tasks.module.featuresettings.LocationSettingsPreferencesImpl
import com.elementary.tasks.module.featuresettings.NoteSettingsPreferencesImpl
import com.elementary.tasks.module.featuresettings.SecuritySettingsPreferencesImpl
import com.elementary.tasks.module.featuresettings.SettingsHubDoNotDisturbCheckerImpl
import com.elementary.tasks.module.featuresettings.SettingsHubRemoteMessagesImpl
import com.elementary.tasks.module.featuresettings.TroubleshootingCacheUtilImpl
import com.elementary.tasks.module.locationapi.LocationTrackingApiImpl
import com.elementary.tasks.module.logicnotificationaction.AppReminderAlertHandlerFactory
import com.elementary.tasks.module.logicnotificationaction.DoNotDisturbPreferencesImpl
import com.elementary.tasks.module.logicnotificationaction.NotificationGatewayImpl
import com.elementary.tasks.module.logicnotificationaction.PhoneCallStateProviderImpl
import com.elementary.tasks.module.logicnotificationaction.WearPreferencesImpl
import com.elementary.tasks.module.logicreminder.AppReminderNotifier
import com.elementary.tasks.module.logicreminder.ReminderPreferencesImpl
import com.elementary.tasks.module.logicschedule.SchedulePreferencesImpl
import com.elementary.tasks.module.platform.BuildInfoImpl
import com.elementary.tasks.module.platform.DateTimePreferencesImpl
import com.elementary.tasks.module.platform.InstallReferrerReader
import com.elementary.tasks.module.sync.SyncDataConverterImpl
import com.elementary.tasks.module.uicommon.AppPreferencesImpl
import com.elementary.tasks.module.uicommon.AuthPreferencesImpl
import com.elementary.tasks.module.uicommon.FontApiImpl
import com.elementary.tasks.module.uicommon.LocalePreferencesImpl
import com.elementary.tasks.module.uicommon.ThemePreferencesImpl
import com.elementary.tasks.module.uimap.MapPreferencesImpl
import com.github.naz013.analytics.AnalyticsStateProvider
import com.github.naz013.analytics.initializeAnalytics
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.singlenote.NoteWidgetPreferences
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.datecalc.DateTimePreferences
import com.github.naz013.feature.googletask.GoogleTasksPreferences
import com.github.naz013.feature.settings.NoteSettingsPreferences
import com.github.naz013.feature.settings.SettingsHubDoNotDisturbChecker
import com.github.naz013.feature.settings.SettingsHubRemoteMessages
import com.github.naz013.feature.settings.calendar.CalendarSettingsPreferences
import com.github.naz013.feature.settings.debug.DeveloperSettingsPreferences
import com.github.naz013.feature.settings.export.CloudBackupSettingsPreferences
import com.github.naz013.feature.settings.general.GeneralSettingsPreferences
import com.github.naz013.feature.settings.location.LocationSettingsPreferences
import com.github.naz013.feature.settings.security.SecuritySettingsPreferences
import com.github.naz013.feature.settings.troubleshooting.TroubleshootingCacheUtil
import com.github.naz013.location.LocationTrackingApi
import com.github.naz013.logic.notificationaction.DoNotDisturbPreferences
import com.github.naz013.logic.notificationaction.NotificationGateway
import com.github.naz013.logic.notificationaction.PhoneCallStateProvider
import com.github.naz013.logic.notificationaction.WearPreferences
import com.github.naz013.logic.notificationaction.reminder.ReminderAlertHandlerFactory
import com.github.naz013.logic.reminder.ReminderNotifier
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.schedule.SchedulePreferences
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.ui.common.font.FontApi
import com.github.naz013.ui.common.locale.LocalePreferences
import com.github.naz013.ui.common.login.AuthPreferences
import com.github.naz013.ui.common.preferences.AppPreferences
import com.github.naz013.ui.common.theme.ThemePreferences
import com.github.naz013.ui.map.MapPreferences
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val libModule = module {
  // platform
  factoryOf<BuildInfo>(::BuildInfoImpl)
  single { DateTimePreferencesImpl(get()) as DateTimePreferences }
  factoryOf(::InstallReferrerReader)

  // ui-common
  single { ThemePreferencesImpl(get()) as ThemePreferences }
  single { LocalePreferencesImpl(get()) as LocalePreferences }
  single { AuthPreferencesImpl(get()) as AuthPreferences }
  factory { FontApiImpl(get()) as FontApi }
  factory { AppPreferencesImpl(get()) as AppPreferences }

  // appwidgets
  single { AppWidgetPreferencesImpl(get()) as AppWidgetPreferences }
  single { NoteWidgetPreferencesImpl(get()) as NoteWidgetPreferences }

  // analytics
  single { initializeAnalytics(get(), get()) }
  factory { AnalyticsStateProviderImpl(get()) as AnalyticsStateProvider }

  // sync
  factory { SyncDataConverterImpl(get()) as SyncDataConverter }

  // feature google task
  factory { GoogleTasksPreferencesImpl(get()) as GoogleTasksPreferences }

  // feature settings
  factory { GeneralSettingsPreferencesImpl(get()) as GeneralSettingsPreferences }
  factory { TroubleshootingCacheUtilImpl(get()) as TroubleshootingCacheUtil }
  factory { SecuritySettingsPreferencesImpl(get()) as SecuritySettingsPreferences }
  factory { LocationSettingsPreferencesImpl(get()) as LocationSettingsPreferences }
  factory { CalendarSettingsPreferencesImpl(get()) as CalendarSettingsPreferences }
  factory { CloudBackupSettingsPreferencesImpl(get()) as CloudBackupSettingsPreferences }
  factory { NoteSettingsPreferencesImpl(get()) as NoteSettingsPreferences }
  factory { DeveloperSettingsPreferencesImpl(get()) as DeveloperSettingsPreferences }
  factory { SettingsHubDoNotDisturbCheckerImpl(get(), get()) as SettingsHubDoNotDisturbChecker }
  factory { SettingsHubRemoteMessagesImpl(get()) as SettingsHubRemoteMessages }

  // logic schedule
  factory { SchedulePreferencesImpl(get()) as SchedulePreferences }

  // logic reminder
  factory { ReminderPreferencesImpl(get()) as ReminderPreferences }
  factory { AppReminderNotifier(get(), get(), get(), get()) as ReminderNotifier }

  // logic notification action
  factory { NotificationGatewayImpl(get()) as NotificationGateway }
  factory { DoNotDisturbPreferencesImpl(get()) as DoNotDisturbPreferences }
  factory { WearPreferencesImpl(get()) as WearPreferences }
  factory { PhoneCallStateProviderImpl(get()) as PhoneCallStateProvider }
  factory {
    AppReminderAlertHandlerFactory(
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
      get(),
    ) as ReminderAlertHandlerFactory
  }

  // location api
  factory { LocationTrackingApiImpl(get()) as LocationTrackingApi }

  // ui-map
  factory { MapPreferencesImpl(get()) as MapPreferences }
}
