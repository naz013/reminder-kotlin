package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.Birthday
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface BirthdaysDao {

    @Query("SELECT * FROM Birthday")
    fun all(): List<Birthday>

    @Query("SELECT * FROM Birthday WHERE uuId=:id")
    fun loadById(id: String): LiveData<Birthday>

    @Query("SELECT * FROM Birthday WHERE uuId=:id")
    fun getById(id: String): Birthday?

    @Query("SELECT * FROM Birthday")
    fun loadAll(): LiveData<List<Birthday>>

    @Query("SELECT * FROM Birthday WHERE dayMonth=:dayMonth")
    fun getAll(dayMonth: String): List<Birthday>

    @Query("SELECT * FROM Birthday WHERE dayMonth=:dayMonth")
    fun loadAll(dayMonth: String): LiveData<List<Birthday>>

    @Query("SELECT * FROM Birthday WHERE dayMonth IN (:dayMonths)")
    fun findAll(dayMonths: List<String>): LiveData<List<Birthday>>

    @Insert(onConflict = REPLACE)
    fun insert(birthday: Birthday)

    @Insert(onConflict = REPLACE)
    suspend fun insertAll(vararg birthdays: Birthday)

    @Delete
    suspend fun delete(birthday: Birthday)

    @Delete
    suspend fun deleteAll(vararg birthdays: Birthday)

    @Query("DELETE FROM Birthday WHERE uuId=:id")
    fun delete(id: String)

    @Query("DELETE FROM Birthday")
    fun deleteAll()
}
