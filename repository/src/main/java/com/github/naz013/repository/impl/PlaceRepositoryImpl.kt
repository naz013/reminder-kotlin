package com.github.naz013.repository.impl

import com.github.naz013.domain.Place
import com.github.naz013.logging.Logger
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.dao.PlacesDao
import com.github.naz013.repository.entity.PlaceEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class PlaceRepositoryImpl(
  private val placesDao: PlacesDao,
  private val tableChangeNotifier: TableChangeNotifier
) : PlaceRepository {

  private val table = Table.Place

  override suspend fun save(place: Place) {
    Logger.d(TAG, "Save place: ${place.id}")
    placesDao.insert(PlaceEntity(place))
    tableChangeNotifier.notify(table)
  }

  override suspend fun getById(id: String): Place? {
    Logger.d(TAG, "Get place by id: $id")
    return placesDao.getById(id)?.toDomain()
  }

  override suspend fun getAll(): List<Place> {
    Logger.d(TAG, "Get all places")
    return placesDao.getAll().map { it.toDomain() }
  }

  override suspend fun searchByName(query: String): List<Place> {
    Logger.d(TAG, "Search place by name: $query")
    return placesDao.searchByName(query).map { it.toDomain() }
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete place by id: $id")
    placesDao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all places")
    placesDao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  companion object {
    private const val TAG = "PlaceRepository"
  }
}
