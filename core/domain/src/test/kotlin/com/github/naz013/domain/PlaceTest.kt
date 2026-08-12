package com.github.naz013.domain

import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceTest {

  @Test
  fun `latLng builds a LatLng from latitude and longitude`() {
    val place = Place(latitude = 12.5, longitude = -3.25, syncState = SyncState.Synced)

    val latLng = place.latLng()

    assertEquals(12.5, latLng.latitude, 0.0)
    assertEquals(-3.25, latLng.longitude, 0.0)
  }
}
