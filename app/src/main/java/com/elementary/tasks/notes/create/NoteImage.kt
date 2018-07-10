package com.elementary.tasks.notes.create

import com.google.gson.annotations.SerializedName

import java.io.Serializable
import java.util.Arrays

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

class NoteImage : Serializable {

    @SerializedName("image")
    private var image: ByteArray? = null

    constructor() {}

    constructor(image: ByteArray) {
        this.image = ByteArray(image.size)
        System.arraycopy(image, 0, this.image!!, 0, this.image!!.size)
    }

    fun getImage(): ByteArray? {
        return image
    }

    fun setImage(image: ByteArray) {
        this.image = ByteArray(image.size)
        System.arraycopy(image, 0, this.image!!, 0, this.image!!.size)
    }

    override fun toString(): String {
        return "NoteImage{" +
                "image=" + Arrays.toString(image) +
                '}'.toString()
    }
}
