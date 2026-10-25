package com.github.naz013.logic.habit.usecase

import com.github.naz013.datecalc.NowDateTimeProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate

private const val MIN_TICK_MS = 1_000L

/** Emits the current local date and again each time it rolls over at midnight. */
internal fun NowDateTimeProvider.observeToday(): Flow<LocalDate> = flow {
  while (true) {
    emit(nowDate())
    val now = nowDateTime()
    val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
    delay(Duration.between(now, nextMidnight).toMillis().coerceAtLeast(MIN_TICK_MS))
  }
}.distinctUntilChanged()
