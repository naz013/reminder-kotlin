package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elementary.tasks.core.utils.SuperUtil
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
class MissedCall {

    @SerializedName("number")
    @PrimaryKey
    var number: String = ""
    @SerializedName("dateTime")
    var dateTime: Long = 0
    @SerializedName("uniqueId")
    var uniqueId: Int = 0

    init {
        this.uniqueId = Random().nextInt(Integer.MAX_VALUE)
    }

    override fun toString(): String {
        return SuperUtil.getObjectPrint(this, MissedCall::class.java)
    }
}
