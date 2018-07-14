package com.elementary.tasks.core.data.models

import com.elementary.tasks.core.data.converters.NoteImagesTypeConverter
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.notes.create.NoteImage
import com.google.gson.annotations.SerializedName

import java.util.ArrayList
import java.util.Random
import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Copyright 2016 Nazar Suhovich
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
@Entity
@TypeConverters(NoteImagesTypeConverter::class)
class Note {

    @SerializedName("summary")
    var summary: String = ""
    @SerializedName("key")
    @PrimaryKey
    var key: String = ""
    @SerializedName("date")
    var date: String = ""
    @SerializedName("color")
    var color: Int = 0
    @SerializedName("style")
    var style: Int = 0
    @SerializedName("images")
    var images: List<NoteImage> = ArrayList()
    @SerializedName("uniqueId")
    var uniqueId: Int = 0

    init {
        this.uniqueId = Random().nextInt(Integer.MAX_VALUE)
        key = UUID.randomUUID().toString()
    }

    override fun toString(): String {
        return SuperUtil.getObjectPrint(this, Note::class.java)
    }
}
