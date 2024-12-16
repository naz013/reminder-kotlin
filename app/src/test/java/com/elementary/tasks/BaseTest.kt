package com.elementary.tasks

import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.github.naz013.logging.Logger
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
    Logger.reportingEnabled = false
    Logger.loggingEnabled = false
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
