package com.github.naz013.repository

import com.github.naz013.domain.Place
import com.github.naz013.domain.sync.SyncState

interface PlaceRepository {
  suspend fun save(place: Place)

  suspend fun getById(id: String): Place?
  suspend fun getAll(): List<Place>
  suspend fun searchByName(query: String): List<Place>

  suspend fun delete(id: String)
  suspend fun deleteAll()

  suspend fun updateSyncState(id: String, state: SyncState)
  suspend fun getIdsByState(syncStates: List<SyncState>): List<String>
}
