package com.elementary.tasks.core.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import javax.inject.Inject

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
abstract class BaseMapFragment : Fragment() {

    @Inject
    lateinit var themeUtil: ThemeUtil
    @Inject
    lateinit var prefs: Prefs
    @Inject
    lateinit var dialogues: Dialogues

    private var mMapType = GoogleMap.MAP_TYPE_TERRAIN

    init {
        ReminderApp.appComponent.inject(this)
    }

    protected fun setStyle(map: GoogleMap, mapType: Int = mMapType) {
        mMapType = mapType
        map.setMapStyle(null)
        if (mapType == 3) {
            val ctx = context ?: return
            val res = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(ctx, themeUtil.mapStyleJson))
            if (!res) {
                map.mapType = mapType
            }
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
