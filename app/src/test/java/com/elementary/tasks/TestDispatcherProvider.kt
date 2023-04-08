package com.elementary.tasks

import com.elementary.tasks.core.utils.DispatcherProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher

fun mockDispatcherProvider(): DispatcherProvider {
  val dispatcherProvider = mockk<DispatcherProvider>()
  every { dispatcherProvider.default() }.returns(Dispatchers.Unconfined)
  every { dispatcherProvider.io() }.returns(Dispatchers.Unconfined)
  every { dispatcherProvider.main() }.returns(Dispatchers.Unconfined)
  return dispatcherProvider
}