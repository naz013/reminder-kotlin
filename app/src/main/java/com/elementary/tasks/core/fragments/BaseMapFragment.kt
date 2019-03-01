package com.elementary.tasks.core.fragments

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.BindingFragment
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import org.koin.android.ext.android.inject

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
abstract class BaseMapFragment<B : ViewDataBinding> : BindingFragment<B>() {

    protected val themeUtil: ThemeUtil by inject()
    protected val prefs: Prefs by inject()
    protected val dialogues: Dialogues by inject()

    private var mMapType = GoogleMap.MAP_TYPE_TERRAIN

    protected fun setStyle(map: GoogleMap, mapType: Int = mMapType) {
        mMapType = mapType
        if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
            if (map.mapType == GoogleMap.MAP_TYPE_SATELLITE || map.mapType == GoogleMap.MAP_TYPE_HYBRID) {
                map.mapType = GoogleMap.MAP_TYPE_NONE
            }
            val ctx = context ?: return
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(ctx, themeUtil.mapStyleJson))
            map.mapType = mapType
        } else {
            map.mapType = mapType
        }
    }

    protected fun setMapType(map: GoogleMap, type: Int, function: (() -> Unit)?) {
        setStyle(map, type)
        prefs.mapType = type
        function?.invoke()
    }

    protected fun refreshStyles(map: GoogleMap) {
        setStyle(map, prefs.mapType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMapType = prefs.mapType
    }
}
