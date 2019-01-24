package com.elementary.tasks.core.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.elementary.tasks.notes.create.DecodeImages
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

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
@Entity
data class ImageFile(
        @SerializedName("image")
        @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
        var image: ByteArray? = null,
        @SerializedName("noteId")
        var noteId: String = "",
        @Transient
        @Ignore
        var state: DecodeImages.State = DecodeImages.State.Ready) : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageFile

        if (!Arrays.equals(image, other.image)) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = image?.let { Arrays.hashCode(it) } ?: 0
        result = 31 * result + id.hashCode()
        return result
    }

    override fun toString(): String {
        return "ImageFile(noteId='$noteId', id=$id)"
    }
}
