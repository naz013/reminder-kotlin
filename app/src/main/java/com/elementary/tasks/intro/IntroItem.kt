package com.elementary.tasks.intro

import androidx.annotation.DrawableRes

/**
 * Copyright 2017 Nazar Suhovich
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

internal class IntroItem(var title: String?, var description: String?, @DrawableRes vararg images: Int) {
    @DrawableRes
    private var images: IntArray? = null

    init {
        this.images = images
    }

    @DrawableRes
    fun getImages(): IntArray? {
        return images
    }

    fun setImages(@DrawableRes images: IntArray) {
        this.images = IntArray(images.size)
        System.arraycopy(images, 0, this.images!!, 0, this.images!!.size)
    }
}
