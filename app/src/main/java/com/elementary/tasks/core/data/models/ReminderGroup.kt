package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.elementary.tasks.core.utils.TimeUtil
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
class ReminderGroup {

    @SerializedName("title")
    var groupTitle: String = ""
    @SerializedName("uuId")
    @PrimaryKey
    var groupUuId: String = ""
    @SerializedName("color")
    var groupColor: Int = 0
    @SerializedName("dateTime")
    var groupDateTime: String = ""
    @SerializedName("isDefaultGroup")
    var isDefaultGroup: Boolean = false

    constructor() {
        this.groupUuId = UUID.randomUUID().toString()
    }

    @Ignore
    constructor(title: String, color: Int) {
        this.groupTitle = title
        this.groupUuId = UUID.randomUUID().toString()
        this.groupColor = color
        this.groupDateTime = TimeUtil.gmtDateTime
    }

    @Ignore
    constructor(title: String, uuId: String, color: Int, dateTime: String) {
        this.groupTitle = title
        this.groupUuId = uuId
        this.groupColor = color
        this.groupDateTime = dateTime
    }

    override fun toString(): String {
        return "ReminderGroup(groupTitle='$groupTitle', groupUuId='$groupUuId', groupColor=$groupColor, groupDateTime='$groupDateTime')"
    }
}
