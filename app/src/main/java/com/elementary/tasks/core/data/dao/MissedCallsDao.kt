package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.MissedCall
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface MissedCallsDao {

    @Query("SELECT * FROM MissedCall")
    fun all(): List<MissedCall>

    @Query("SELECT * FROM MissedCall")
    fun loadAll(): LiveData<List<MissedCall>>

    @Insert(onConflict = REPLACE)
    fun insert(missedCall: MissedCall)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg missedCalls: MissedCall)

    @Delete
    fun delete(missedCall: MissedCall)

    @Query("SELECT * FROM MissedCall WHERE number=:number")
    fun loadByNumber(number: String): LiveData<MissedCall>

    @Query("SELECT * FROM MissedCall WHERE number=:number")
    fun getByNumber(number: String): MissedCall?
}
