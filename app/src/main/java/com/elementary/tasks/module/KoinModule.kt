package com.elementary.tasks.module

import com.elementary.tasks.module.analytics.AnalyticsStateProviderImpl
import com.elementary.tasks.module.analytics.ReminderAnalyticsTracker
import com.elementary.tasks.module.appwidgets.AppWidgetPreferencesImpl
import com.elementary.tasks.module.appwidgets.NoteWidgetPreferencesImpl
import com.elementary.tasks.module.featuregoogletask.GoogleTasksPreferencesImpl
import com.elementary.tasks.module.locationapi.LocationTrackingApiImpl
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
import com.github.naz013.analytics.AnalyticsStateProvider
import com.github.naz013.analytics.initializeAnalytics
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.singlenote.NoteWidgetPreferences
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.datecalc.DateTimePreferences
import com.github.naz013.feature.googletask.GoogleTasksPreferences
import com.github.naz013.location.LocationTrackingApi
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.schedule.SchedulePreferences
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.ui.common.font.FontApi
import com.github.naz013.ui.common.locale.LocalePreferences
import com.github.naz013.ui.common.login.AuthPreferences
import com.github.naz013.ui.common.preferences.AppPreferences
import com.github.naz013.ui.common.theme.ThemePreferences
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
  factory { ReminderAnalyticsTracker(get()) }
  factory { AnalyticsStateProviderImpl(get()) as AnalyticsStateProvider }

  // sync
  factory { SyncDataConverterImpl(get()) as SyncDataConverter }

  // feature google task
  factory { GoogleTasksPreferencesImpl(get()) as GoogleTasksPreferences }

  // logic schedule
  factory { SchedulePreferencesImpl(get()) as SchedulePreferences }

  // logic reminder
  factory { ReminderPreferencesImpl(get()) as ReminderPreferences }

  // location api
  factory { LocationTrackingApiImpl(get()) as LocationTrackingApi }
}
