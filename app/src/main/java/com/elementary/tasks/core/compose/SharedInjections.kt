package com.elementary.tasks.core.compose

import androidx.compose.runtime.Composable
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.reviews.ReviewsApi
import org.koin.compose.koinInject

/**
 * Compose-friendly accessors for cross-cutting Koin singletons used directly by several
 * `XyzNavGraph.kt` Entry composables. NavGraph Entries call these (or a `koinViewModel<>()`)
 * instead of `koinInject<>()` directly, keeping the DI lookup expressed as a composable like every
 * other Entry-level dependency (permission requesters, dialog/toast dispatchers, launchers, ...).
 */
@Composable
fun rememberPrefs(): Prefs = koinInject()

@Composable
fun rememberDateTimeManager(): DateTimeManager = koinInject()

@Composable
fun rememberAnalyticsEventSender(): AnalyticsEventSender = koinInject()

@Composable
fun rememberFeatureManager(): FeatureManager = koinInject()

@Composable
fun rememberReviewsApi(): ReviewsApi = koinInject()

@Composable
fun rememberAppWidgetUpdater(): AppWidgetUpdater = koinInject()

@Composable
fun rememberGoogleCalendarUtils(): GoogleCalendarUtils = koinInject()

@Composable
fun rememberPackageManagerWrapper(): PackageManagerWrapper = koinInject()
