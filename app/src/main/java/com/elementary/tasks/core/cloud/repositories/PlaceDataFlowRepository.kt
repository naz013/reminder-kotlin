package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Place

class PlaceDataFlowRepository(appDb: AppDb) : DatabaseRepository<Place>(appDb) {
  override suspend fun get(id: String): Place? {
    return appDb.placesDao().getByKey(id)
  }

  override suspend fun insert(t: Place) {
    appDb.placesDao().insert(t)
  }

  override suspend fun all(): List<Place> {
    return appDb.placesDao().getAll()
  }

  override suspend fun delete(t: Place) {
    appDb.placesDao().delete(t)
  }
}
