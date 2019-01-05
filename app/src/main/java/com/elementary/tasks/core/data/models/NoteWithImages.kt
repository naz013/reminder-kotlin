package com.elementary.tasks.core.data.models

import androidx.room.Embedded
import androidx.room.Relation
import com.elementary.tasks.core.interfaces.NoteInterface
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
data class NoteWithImages(
        @Embedded
        var note: Note? = null,
        @Relation(parentColumn = "key", entityColumn = "noteId")
        var images: List<ImageFile> = ArrayList()
) : NoteInterface {

    override fun getGmtTime(): String {
        return note?.date ?: ""
    }

    override fun getSummary(): String {
        return note?.summary ?: ""
    }

    override fun getKey(): String {
        return note?.key ?: ""
    }

    override fun getColor(): Int {
        return note?.color ?: 0
    }

    override fun getStyle(): Int {
        return note?.style ?: 0
    }

    override fun getOpacity(): Int {
        return note?.opacity ?: 0
    }
}
