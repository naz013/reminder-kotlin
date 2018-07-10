package com.elementary.tasks.core.data.models

import com.google.gson.annotations.SerializedName

import java.util.LinkedList

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

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
class MainImage : Observable {

    @SerializedName("format")
    @get:Bindable
    var format: String? = null
    @SerializedName("width")
    @get:Bindable
    var width: Int = 0
    @SerializedName("height")
    @get:Bindable
    var height: Int = 0
    @SerializedName("filename")
    @get:Bindable
    var filename: String? = null
    @SerializedName("id")
    @PrimaryKey
    @get:Bindable
    var id: Long = 0
    @SerializedName("author")
    @get:Bindable
    var author: String? = null
    @SerializedName("author_url")
    @get:Bindable
    var authorUrl: String? = null
    @SerializedName("post_url")
    @get:Bindable
    var postUrl: String? = null

    @Ignore
    private val mCallbacks = LinkedList<Observable.OnPropertyChangedCallback>()

    override fun addOnPropertyChangedCallback(onPropertyChangedCallback: Observable.OnPropertyChangedCallback) {
        if (!mCallbacks.contains(onPropertyChangedCallback)) {
            mCallbacks.add(onPropertyChangedCallback)
        }
    }

    override fun removeOnPropertyChangedCallback(onPropertyChangedCallback: Observable.OnPropertyChangedCallback) {
        mCallbacks.remove(onPropertyChangedCallback)
    }
}
