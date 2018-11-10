package com.elementary.tasks.navigation.settings.location

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatRadioButton
import com.elementary.tasks.R
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

        styleDay.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
        styleAubergine.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
        styleAuto.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
        styleDark.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
        styleNight.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
        styleRetro.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
        styleSilver.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }

        selectCurrent(prefs.mapStyle)
    }

    private fun selectCurrent(mapStyle: Int) {
        when (mapStyle) {
            1 -> styleRetro.isChecked = true
            2 -> styleSilver.isChecked = true
            3 -> styleNight.isChecked = true
            4 -> styleDark.isChecked = true
            5 -> styleAubergine.isChecked = true
            6 -> styleAuto.isChecked = true
            else -> styleDay.isChecked = true
        }
    }

    private fun invoke(v: View, isChecked: Boolean) {
        if (!isChecked) return
        buttons().forEach {
            if (v.id != it.id) {
                it.isChecked = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        prefs.mapStyle = selection
    }

    private fun buttons(): List<AppCompatRadioButton> {
        return listOf(styleDay, styleAubergine, styleAuto, styleDark, styleNight, styleRetro, styleSilver)
    }

    override fun getTitle(): String = getString(R.string.map_style)

    companion object {

        fun newInstance(): MapStyleFragment = MapStyleFragment()
    }
}
