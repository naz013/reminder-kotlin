package com.elementary.tasks

import com.github.naz013.feature.common.coroutine.DispatcherProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers

fun mockDispatcherProvider(): DispatcherProvider {
  val dispatcherProvider = mockk<DispatcherProvider>()
  every { dispatcherProvider.default() }.returns(Dispatchers.Unconfined)
  every { dispatcherProvider.io() }.returns(Dispatchers.Unconfined)
  every { dispatcherProvider.main() }.returns(Dispatchers.Unconfined)
  return dispatcherProvider
}
