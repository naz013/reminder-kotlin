package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.SmsTemplate
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface PlacesDao {

    @Query("SELECT * FROM Place")
    fun all(): List<Place>

    @Query("SELECT * FROM Place")
    fun loadAll(): LiveData<List<Place>>

    @Insert(onConflict = REPLACE)
    fun insert(place: Place)

    @Insert(onConflict = REPLACE)
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
