package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elementary.tasks.core.data.models.Place

@Dao
interface PlacesDao {

    @Query("SELECT * FROM Place")
    fun getAll(): List<Place>

    @Query("SELECT * FROM Place WHERE LOWER(name) LIKE '%' || :query || '%'")
    fun searchByName(query: String): List<Place>

    @Query("SELECT * FROM Place")
    fun loadAll(): LiveData<List<Place>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(place: Place)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg places: Place)

    @Delete
    fun delete(place: Place)

    @Query("SELECT * FROM Place WHERE id=:id")
    fun loadByKey(id: String): LiveData<Place>

    @Query("SELECT * FROM Place WHERE id=:id")
    fun getByKey(id: String): Place?

    @Query("DELETE FROM Place")
    fun deleteAll()
}
