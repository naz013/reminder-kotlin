package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [(Index("id")), (Index("timeString")), (Index("timeMills"))])
data class UsedTime(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,
        var timeString: String = "",
        var timeMills: Long = 0,
        var useCount: Int = 0
)