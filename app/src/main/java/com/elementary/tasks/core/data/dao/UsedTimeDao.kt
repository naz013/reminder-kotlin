package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.elementary.tasks.core.data.models.UsedTime

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    @Insert(onConflict = REPLACE)
    fun insert(usedTime: UsedTime)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg usedTimes: UsedTime)

    @Delete
    fun delete(usedTime: UsedTime)

    @Query("DELETE FROM UsedTime")
    fun deleteAll()
}
