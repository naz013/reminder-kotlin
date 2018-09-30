package com.elementary.tasks.navigation.settings.location

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import com.elementary.tasks.R
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_marker_style.*

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
class MarkerStyleFragment : BaseSettingsFragment() {

    private val listener1: RadioGroup.OnCheckedChangeListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup2.setOnCheckedChangeListener(null)
            themeGroup3.setOnCheckedChangeListener(null)
            themeGroup4.setOnCheckedChangeListener(null)
            themeGroup2.clearCheck()
            themeGroup3.clearCheck()
            themeGroup4.clearCheck()
            themeGroup2.setOnCheckedChangeListener(listener2)
            themeGroup3.setOnCheckedChangeListener(listener3)
            themeGroup4.setOnCheckedChangeListener(listener4)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }
    private val listener2: RadioGroup.OnCheckedChangeListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup.setOnCheckedChangeListener(null)
            themeGroup3.setOnCheckedChangeListener(null)
            themeGroup4.setOnCheckedChangeListener(null)
            themeGroup.clearCheck()
            themeGroup3.clearCheck()
            themeGroup4.clearCheck()
            themeGroup.setOnCheckedChangeListener(listener1)
            themeGroup3.setOnCheckedChangeListener(listener3)
            themeGroup4.setOnCheckedChangeListener(listener4)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }
    private val listener3: RadioGroup.OnCheckedChangeListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup.setOnCheckedChangeListener(null)
            themeGroup2.setOnCheckedChangeListener(null)
            themeGroup4.setOnCheckedChangeListener(null)
            themeGroup.clearCheck()
            themeGroup2.clearCheck()
            themeGroup4.clearCheck()
            themeGroup.setOnCheckedChangeListener(listener1)
            themeGroup2.setOnCheckedChangeListener(listener2)
            themeGroup4.setOnCheckedChangeListener(listener4)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }
    private val listener4: RadioGroup.OnCheckedChangeListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup.setOnCheckedChangeListener(null)
            themeGroup2.setOnCheckedChangeListener(null)
            themeGroup3.setOnCheckedChangeListener(null)
            themeGroup.clearCheck()
            themeGroup2.clearCheck()
            themeGroup3.clearCheck()
            themeGroup.setOnCheckedChangeListener(listener1)
            themeGroup2.setOnCheckedChangeListener(listener2)
            themeGroup3.setOnCheckedChangeListener(listener3)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }

    override fun layoutRes(): Int = R.layout.fragment_settings_marker_style

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActions()
        setUpRadio()
    }

    private fun initActions() {
        themeGroup.clearCheck()
        themeGroup2.clearCheck()
        themeGroup3.clearCheck()
        themeGroup4.clearCheck()
        themeGroup.setOnCheckedChangeListener(listener1)
        themeGroup2.setOnCheckedChangeListener(listener2)
        themeGroup3.setOnCheckedChangeListener(listener3)
        themeGroup4.setOnCheckedChangeListener(listener4)
    }

    private fun setUpRadio() {
        val loaded = prefs.markerStyle
        when (loaded) {
            0 -> redCheck.isChecked = true
            1 -> purpleCheck.isChecked = true
            2 -> greenLightCheck.isChecked = true
            3 -> greenCheck.isChecked = true
            4 -> blueLightCheck.isChecked = true
            5 -> blueCheck.isChecked = true
            6 -> yellowCheck.isChecked = true
            7 -> orangeCheck.isChecked = true
            8 -> cyanCheck.isChecked = true
            9 -> pinkCheck.isChecked = true
            10 -> tealCheck.isChecked = true
            11 -> amberCheck.isChecked = true
            12 -> deepPurpleCheck.isChecked = true
            13 -> deepOrangeCheck.isChecked = true
            14 -> limeCheck.isChecked = true
            15 -> indigoCheck.isChecked = true
            else -> blueCheck.isChecked = true
        }
    }

    private fun themeColorSwitch(radio: Int) {
        when (radio) {
            R.id.redCheck -> saveColor(0)
            R.id.purpleCheck -> saveColor(1)
            R.id.greenLightCheck -> saveColor(2)
            R.id.greenCheck -> saveColor(3)
            R.id.blueLightCheck -> saveColor(4)
            R.id.blueCheck -> saveColor(5)
            R.id.yellowCheck -> saveColor(6)
            R.id.orangeCheck -> saveColor(7)
            R.id.cyanCheck -> saveColor(8)
            R.id.pinkCheck -> saveColor(9)
            R.id.tealCheck -> saveColor(10)
            R.id.amberCheck -> saveColor(11)
            R.id.deepPurpleCheck -> saveColor(12)
            R.id.deepOrangeCheck -> saveColor(13)
            R.id.limeCheck -> saveColor(14)
            R.id.indigoCheck -> saveColor(15)
        }
    }

    private fun saveColor(style: Int) {
        prefs.markerStyle = style
    }

    override fun getTitle(): String = getString(R.string.style_of_marker)
}
