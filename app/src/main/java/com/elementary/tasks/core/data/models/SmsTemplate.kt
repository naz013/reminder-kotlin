package com.elementary.tasks.core.data.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.elementary.tasks.core.utils.TimeUtil
import com.google.gson.annotations.SerializedName
import java.io.Serializable
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
@Keep
class SmsTemplate : Serializable {
    @SerializedName("title")
    var title: String = ""
    @SerializedName("key")
    @PrimaryKey
    var key: String = ""
    @SerializedName("date")
    var date: String = ""
    @Transient
    var isSelected: Boolean = false

    constructor() {
        this.key = UUID.randomUUID().toString()
    }

    @Ignore
    constructor(title: String, date: String) {
        this.title = title
        this.date = date
        this.key = UUID.randomUUID().toString()
    }

    @Ignore
    constructor(title: String) {
        this.title = title
        this.date = TimeUtil.gmtDateTime
        this.key = UUID.randomUUID().toString()
    }
}
