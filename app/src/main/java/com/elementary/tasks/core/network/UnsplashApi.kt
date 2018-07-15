package com.elementary.tasks.core.network

import com.elementary.tasks.core.data.models.MainImage

import retrofit2.Call
import retrofit2.http.GET

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
interface UnsplashApi {

    @get:GET("list")
    val allImages: Call<List<MainImage>>

    companion object {

        const val BASE_URL = "https://unsplash.it"
    }
}
