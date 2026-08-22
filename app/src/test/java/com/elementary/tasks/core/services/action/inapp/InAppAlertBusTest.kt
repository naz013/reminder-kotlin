package com.elementary.tasks.core.services.action.inapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InAppAlertBusTest {
  private val bus = InAppAlertBus()

  private fun alert(id: String) =
    InAppAlert(
      alertId = id,
      domain = InAppAlertDomain.REMINDER,
      title = "title-$id",
      text = null,
      iconRes = 0,
      actions = emptyList(),
    )

  @Test
  fun `show publishes the alert as current`() {
    bus.show(alert("1"))

    assertEquals("1", bus.current.value?.alertId)
  }

  @Test
  fun `a newer show replaces the previous alert instead of stacking`() {
    bus.show(alert("1"))
    bus.show(alert("2"))

    assertEquals("2", bus.current.value?.alertId)
  }

  @Test
  fun `clear removes the current alert when the id matches`() {
    bus.show(alert("1"))

    bus.clear("1")

    assertNull(bus.current.value)
  }

  @Test
  fun `clear is a no-op when the id no longer matches the current alert`() {
    bus.show(alert("1"))
    bus.show(alert("2"))

    bus.clear("1")

    assertEquals("2", bus.current.value?.alertId)
  }
}
