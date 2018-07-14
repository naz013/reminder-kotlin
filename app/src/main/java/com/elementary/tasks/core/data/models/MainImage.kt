package com.elementary.tasks.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
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
@Entity
class MainImage {

    @SerializedName("format")
    var format: String? = null
    @SerializedName("width")
    var width: Int = 0
    @SerializedName("height")
    var height: Int = 0
    @SerializedName("filename")
    var filename: String? = null
    @SerializedName("id")
    @PrimaryKey
    var id: Long = 0
    @SerializedName("author")
    var author: String? = null
    @SerializedName("author_url")
    var authorUrl: String? = null
    @SerializedName("post_url")
    var postUrl: String? = null
}
