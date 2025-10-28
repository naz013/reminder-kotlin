package com.github.naz013.sync.local

import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.PlaceRepository

internal class PlaceRepositoryCaller(
  private val placeRepository: PlaceRepository
) : DataTypeRepositoryCaller<Place> {
  override suspend fun getById(id: String): Place? {
    return placeRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return placeRepository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    placeRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    if (item !is Place) {
      throw IllegalArgumentException("Invalid item type: ${item::class.java}, expected: Place")
    }
    placeRepository.save(item)
  }

  override suspend fun getAllIds(): List<String> {
    return placeRepository.getAllIds()
  }
}
