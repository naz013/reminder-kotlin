package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elementary.tasks.core.data.models.MissedCall

@Deprecated("After S")
@Dao
interface MissedCallsDao {

    @Query("SELECT * FROM MissedCall")
    fun all(): List<MissedCall>

    @Query("SELECT * FROM MissedCall")
    fun loadAll(): LiveData<List<MissedCall>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(missedCall: MissedCall)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg missedCalls: MissedCall)

    @Delete
    fun delete(missedCall: MissedCall)

    @Query("SELECT * FROM MissedCall WHERE number=:number")
    fun loadByNumber(number: String): LiveData<MissedCall>

    @Query("SELECT * FROM MissedCall WHERE number=:number")
    fun getByNumber(number: String): MissedCall?
}
