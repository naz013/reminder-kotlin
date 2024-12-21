package com.elementary.tasks.core.cloud.repositories

import com.github.naz013.domain.Place
import com.github.naz013.repository.PlaceRepository

class PlaceDataFlowRepository(
  private val placeRepository: PlaceRepository
) : DatabaseRepository<Place>() {
  override suspend fun get(id: String): Place? {
    return placeRepository.getById(id)
  }

  override suspend fun insert(t: Place) {
    placeRepository.save(t)
  }

  override suspend fun all(): List<Place> {
    return placeRepository.getAll()
  }

  override suspend fun delete(t: Place) {
    placeRepository.delete(t.id)
  }
}
