package com.github.naz013.feature.common.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

fun <T> MutableStateFlow<T>.viewModelStateIn(
  scope: CoroutineScope,
  initialValue: T,
  started: SharingStarted = SharingStarted.WhileSubscribed(5000L),
): StateFlow<T> {
  return stateIn(scope, started, initialValue)
}
