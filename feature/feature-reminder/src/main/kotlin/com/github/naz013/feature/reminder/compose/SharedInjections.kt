package com.github.naz013.feature.reminder.compose

import androidx.compose.runtime.Composable
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.googlecalendar.GoogleCalendarApi
import org.koin.compose.koinInject

/**
 * Compose-friendly accessors for cross-cutting Koin singletons used directly by several
 * `XyzNavGraph.kt` Entry composables. NavGraph Entries call these (or a `koinViewModel<>()`)
 * instead of `koinInject<>()` directly, keeping the DI lookup expressed as a composable like every
 * other Entry-level dependency (permission requesters, dialog/toast dispatchers, launchers, ...).
 */

@Composable
fun rememberDateTimeManager(): DateTimeManager = koinInject()

@Composable
fun rememberGoogleCalendarApi(): GoogleCalendarApi = koinInject()

@Composable
fun rememberPackageManagerWrapper(): PackageManagerWrapper = koinInject()
