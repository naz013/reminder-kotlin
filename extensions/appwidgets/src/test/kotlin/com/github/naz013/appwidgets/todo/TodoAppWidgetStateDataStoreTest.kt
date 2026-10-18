package com.github.naz013.appwidgets.todo

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TodoAppWidgetStateDataStoreTest {

  private val viewModel = mockk<TodoAppWidgetViewModel>()

  private fun createDataStore() = TodoAppWidgetStateDataStore(viewModel)

  @Test
  fun `data emits the state computed by the view model`() = runTest {
    val expected = TodoAppWidgetState(widgetId = 7, reminderId = "r1", title = "Groceries")
    coEvery { viewModel.getState() } returns expected

    val emitted = createDataStore().data.first()

    assertEquals(expected, emitted)
  }

  @Test
  fun `data emits empty state when view model reports no reminder`() = runTest {
    coEvery { viewModel.getState() } returns TodoAppWidgetState(widgetId = 7)

    val emitted = createDataStore().data.first()

    assertNull(emitted.reminderId)
    assertEquals(emptyList<Any>(), emitted.items)
  }

  @Test
  fun `update data passes the current state to the transform and returns its result`() = runTest {
    val current = TodoAppWidgetState(widgetId = 7, reminderId = "r1", title = "Groceries")
    val transformed = current.copy(title = "Renamed")
    coEvery { viewModel.getState() } returns current

    val result = createDataStore().updateData { state ->
      assertEquals(current, state)
      transformed
    }

    assertEquals(transformed, result)
    coVerify(exactly = 1) { viewModel.getState() }
  }
}
