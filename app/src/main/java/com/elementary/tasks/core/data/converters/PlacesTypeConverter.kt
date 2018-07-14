package com.elementary.tasks.core.data.converters

import com.elementary.tasks.core.data.models.Place
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import androidx.room.TypeConverter

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
class PlacesTypeConverter {

    @TypeConverter
    fun toJson(list: List<Place>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toList(json: String): List<Place>? {
        return Gson().fromJson<List<Place>>(json, object : TypeToken<List<Place>>() {}.type)
    }
}
