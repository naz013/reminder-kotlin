package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.MainImage

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
interface MainImagesDao {

    @Query("SELECT * FROM MainImage")
    fun loadAll(): LiveData<List<MainImage>>

    @Insert(onConflict = REPLACE)
    fun insert(mainImage: MainImage)

    @Insert(onConflict = REPLACE)
    fun insertAll(mainImages: List<MainImage>)

    @Delete
    fun delete(mainImage: MainImage)
}
