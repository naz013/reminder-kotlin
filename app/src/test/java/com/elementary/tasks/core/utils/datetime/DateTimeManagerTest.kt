package com.elementary.tasks.core.utils.datetime

import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.params.Prefs
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before

class DateTimeManagerTest {

  private val prefs = mockk<Prefs>()
  private val textProvider = mockk<TextProvider>()
  private val dateTimeManager = DateTimeManager(prefs, textProvider)

  @Before
  fun setUp() {
    every { prefs.appLanguage } returns 1
  }


}