package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.elementary.tasks.core.data.models.ReminderChain

@Dao
interface ReminderChainDao {

    @Transaction
    @Query("SELECT * FROM ReminderChain ORDER BY gmtTime DESC")
    fun all(): List<ReminderChain>

    @Transaction
    @Query("SELECT * FROM ReminderChain ORDER BY gmtTime DESC")
    fun loadAll(): LiveData<List<ReminderChain>>

    @Transaction
    @Insert(onConflict = REPLACE)
    fun insert(reminderChain: ReminderChain)

    @Insert(onConflict = REPLACE)
    fun insertAll(reminderChains: List<ReminderChain>)

    @Delete
    fun delete(reminderChain: ReminderChain)

    @Query("SELECT * FROM ReminderChain WHERE uuId=:uuId")
    fun loadById(uuId: String): LiveData<ReminderChain>

    @Query("SELECT * FROM ReminderChain WHERE uuId=:uuId")
    fun getById(uuId: String): ReminderChain?

    @Query("SELECT * FROM ReminderChain WHERE previousId=:uuId")
    fun loadByPreviusId(uuId: String): LiveData<List<ReminderChain>>

    @Query("SELECT * FROM ReminderChain WHERE previousId=:uuId")
    fun getByPreviusId(uuId: String): List<ReminderChain>

    @Query("SELECT * FROM ReminderChain WHERE nextId=:uuId")
    fun loadByNextId(uuId: String): LiveData<List<ReminderChain>>

    @Query("SELECT * FROM ReminderChain WHERE nextId=:uuId")
    fun getByNextId(uuId: String): List<ReminderChain>
}
