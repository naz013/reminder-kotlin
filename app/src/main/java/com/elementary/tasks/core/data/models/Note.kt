package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
data class Note (
        var summary: String = "",
        @PrimaryKey
        var key: String = UUID.randomUUID().toString(),
        var date: String = "",
        var color: Int = 0,
        var style: Int = 0,
        var palette: Int = 0,
        var uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
        var opacity: Int = 100) : Serializable {

    @Ignore
    constructor(oldNote: OldNote) : this() {
        this.color = oldNote.color
        this.palette = oldNote.palette
        this.key = oldNote.key
        this.date = oldNote.date
        this.style = oldNote.style
        this.uniqueId = oldNote.uniqueId
        this.summary = oldNote.summary
    }
}
