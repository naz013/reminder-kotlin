package com.elementary.tasks.core.utils

import android.os.Build

import com.elementary.tasks.BuildConfig

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

object Module {

    val isPro: Boolean
        get() = BuildConfig.IS_PRO

    val isO: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    val isLollipop: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    val isMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    val isNougat: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    val isKitkat: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    val isJellyMR2: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2

    val isJellyMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1

    val isJelly: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
}
