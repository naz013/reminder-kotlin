package com.elementary.tasks.core.cloud.repositories

import com.elementary.tasks.core.data.models.Place

class PlaceRepository : DatabaseRepository<Place>() {
    override suspend fun get(id: String): Place? {
        return appDb.placesDao().getByKey(id)
    }

    override suspend fun insert(t: Place) {
        appDb.placesDao().insert(t)
    }
}