package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elementary.tasks.core.data.models.UsedTime

@Dao
interface UsedTimeDao {

    @Query("SELECT * FROM UsedTime ORDER BY useCount DESC")
    fun loadAll(): LiveData<List<UsedTime>>

    @Query("SELECT * FROM UsedTime WHERE useCount > 1 ORDER BY useCount DESC LIMIT 5")
    fun loadFirst5(): LiveData<List<UsedTime>>

    @Query("SELECT * FROM UsedTime WHERE timeString=:timeString")
    fun getByTimeString(timeString: String): UsedTime?

    @Query("SELECT * FROM UsedTime WHERE timeMills=:timeMills")
    fun getByTimeMills(timeMills: Long): UsedTime?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(usedTime: UsedTime)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg usedTimes: UsedTime)

    @Delete
    fun delete(usedTime: UsedTime)

    @Query("DELETE FROM UsedTime")
    fun deleteAll()
}
