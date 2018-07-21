package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.MissedCall
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import androidx.room.OnConflictStrategy.REPLACE

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
