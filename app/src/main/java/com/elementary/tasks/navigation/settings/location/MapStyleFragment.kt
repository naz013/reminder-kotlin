package com.elementary.tasks.navigation.settings.location

import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.views.roboto.RoboRadioButton
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_map_style.*

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
class MapStyleFragment : BaseSettingsFragment() {

    private val selection: Int
        get() {
            return when {
                styleAuto.isChecked -> 6
                styleDay.isChecked -> 0
                styleRetro.isChecked -> 1
                styleSilver.isChecked -> 2
                styleNight.isChecked -> 3
                styleDark.isChecked -> 4
                styleAubergine.isChecked -> 5
                else -> 0
            }
        }

    override fun layoutRes(): Int = R.layout.fragment_settings_map_style

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        styleDay.setOnClickListener { this.invoke(it) }
        styleAubergine.setOnClickListener { this.invoke(it) }
        styleAuto.setOnClickListener { this.invoke(it) }
        styleDark.setOnClickListener { this.invoke(it) }
        styleNight.setOnClickListener { this.invoke(it) }
        styleRetro.setOnClickListener { this.invoke(it) }
        styleSilver.setOnClickListener { this.invoke(it) }

        styleDay.callOnClick()

        selectCurrent(prefs.mapStyle)
    }

    private fun selectCurrent(mapStyle: Int) {
        when (mapStyle) {
            0 -> styleDay.callOnClick()
            1 -> styleRetro.callOnClick()
            2 -> styleSilver.callOnClick()
            3 -> styleNight.callOnClick()
            4 -> styleDark.callOnClick()
            5 -> styleAubergine.callOnClick()
            6 -> styleAuto.callOnClick()
        }
    }

    private operator fun invoke(v: View) {
        clearChecks()
        if (v is RoboRadioButton) {
            v.isChecked = true
        }
    }

    private fun clearChecks() {
        styleDay.isChecked = false
        styleAubergine.isChecked = false
        styleAuto.isChecked = false
        styleDark.isChecked = false
        styleNight.isChecked = false
        styleRetro.isChecked = false
        styleSilver.isChecked = false
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.mapStyle = selection
    }

    override fun getTitle(): String = getString(R.string.map_style)

    companion object {

        fun newInstance(): MapStyleFragment {
            return MapStyleFragment()
        }
    }
}
