package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

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
class Note() {

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
    @SerializedName("uniqueId")
    var uniqueId: Int = 0

    @Ignore
    constructor(oldNote: OldNote): this() {
        this.color = oldNote.color
        this.key = oldNote.key
        this.date = oldNote.date
        this.style = oldNote.style
        this.uniqueId = oldNote.uniqueId
    }

    init {
        this.uniqueId = Random().nextInt(Integer.MAX_VALUE)
        key = UUID.randomUUID().toString()
    }

    override fun toString(): String {
        return "Note(summary='$summary', key='$key', color=$color, style=$style, uniqueId=$uniqueId)"
    }
}
