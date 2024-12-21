package com.github.naz013.feature.common.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class DispatcherProvider {
  fun main(): CoroutineDispatcher = Dispatchers.Main
  fun io(): CoroutineDispatcher = Dispatchers.IO
  fun default(): CoroutineDispatcher = Dispatchers.Default
}
