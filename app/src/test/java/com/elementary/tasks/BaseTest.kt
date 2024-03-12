package com.elementary.tasks

import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.elementary.tasks.core.analytics.Logger
import com.elementary.tasks.core.analytics.Traces
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseTest {

  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  @Before
  open fun setUp() {
    Traces.reportingEnabled = false
    Traces.logger = object : Logger {
      override fun info(message: String) {
        println(message)
      }

      override fun debug(message: String) {
        println(message)
      }
    }
    Dispatchers.setMain(Dispatchers.Unconfined)
    mockkStatic(Looper::class)
    val looper = mockk<Looper> {
      every { thread } returns Thread.currentThread()
    }
    every { Looper.getMainLooper() } returns looper
  }

  @After
  open fun tearDown() {
  }
}
