package com.elementary.tasks.core.data.models

import com.google.gson.annotations.SerializedName

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
data class OldNote(
        @SerializedName("summary")
        var summary: String = "",
        @SerializedName("key")
        var key: String = "",
        @SerializedName("date")
        var date: String = "",
        @SerializedName("color")
        var color: Int = 0,
        @SerializedName("style")
        var style: Int = 0,
        @SerializedName("images")
        var images: List<ImageFile> = ArrayList(),
        @SerializedName("uniqueId")
        var uniqueId: Int = 0) {

    constructor(noteWithImages: NoteWithImages) : this() {
        this.images = noteWithImages.images
        val note = noteWithImages.note ?: return
        this.uniqueId = note.uniqueId
        this.style = note.style
        this.color = note.color
        this.date = note.date
        this.key = note.key
        this.summary = note.summary
    }
}