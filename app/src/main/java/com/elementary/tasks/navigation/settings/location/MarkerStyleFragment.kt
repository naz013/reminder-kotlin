package com.elementary.tasks.navigation.settings.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.views.roboto.RoboRadioButton
import com.elementary.tasks.databinding.FragmentMarkerStyleBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

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

    private var binding: FragmentMarkerStyleBinding? = null
    private var red: RoboRadioButton? = null
    private var green: RoboRadioButton? = null
    private var blue: RoboRadioButton? = null
    private var yellow: RoboRadioButton? = null
    private var greenLight: RoboRadioButton? = null
    private var blueLight: RoboRadioButton? = null
    private var cyan: RoboRadioButton? = null
    private var purple: RoboRadioButton? = null
    private var amber: RoboRadioButton? = null
    private var orange: RoboRadioButton? = null
    private var pink: RoboRadioButton? = null
    private var teal: RoboRadioButton? = null
    private var deepPurple: RoboRadioButton? = null
    private var deepOrange: RoboRadioButton? = null
    private var indigo: RoboRadioButton? = null
    private var lime: RoboRadioButton? = null
    private var themeGroup: RadioGroup? = null
    private var themeGroup2: RadioGroup? = null
    private var themeGroup3: RadioGroup? = null
    private var themeGroup4: RadioGroup? = null

    private val listener1 = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup2!!.setOnCheckedChangeListener(null)
            themeGroup3!!.setOnCheckedChangeListener(null)
            themeGroup4!!.setOnCheckedChangeListener(null)
            themeGroup2!!.clearCheck()
            themeGroup3!!.clearCheck()
            themeGroup4!!.clearCheck()
            themeGroup2!!.setOnCheckedChangeListener(listener2)
            themeGroup3!!.setOnCheckedChangeListener(listener3)
            themeGroup4!!.setOnCheckedChangeListener(listener4)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }
    private val listener2 = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup!!.setOnCheckedChangeListener(null)
            themeGroup3!!.setOnCheckedChangeListener(null)
            themeGroup4!!.setOnCheckedChangeListener(null)
            themeGroup!!.clearCheck()
            themeGroup3!!.clearCheck()
            themeGroup4!!.clearCheck()
            themeGroup!!.setOnCheckedChangeListener(listener1)
            themeGroup3!!.setOnCheckedChangeListener(listener3)
            themeGroup4!!.setOnCheckedChangeListener(listener4)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }
    private val listener3 = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup!!.setOnCheckedChangeListener(null)
            themeGroup2!!.setOnCheckedChangeListener(null)
            themeGroup4!!.setOnCheckedChangeListener(null)
            themeGroup!!.clearCheck()
            themeGroup2!!.clearCheck()
            themeGroup4!!.clearCheck()
            themeGroup!!.setOnCheckedChangeListener(listener1)
            themeGroup2!!.setOnCheckedChangeListener(listener2)
            themeGroup4!!.setOnCheckedChangeListener(listener4)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }
    private val listener4 = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup!!.setOnCheckedChangeListener(null)
            themeGroup2!!.setOnCheckedChangeListener(null)
            themeGroup3!!.setOnCheckedChangeListener(null)
            themeGroup!!.clearCheck()
            themeGroup2!!.clearCheck()
            themeGroup3!!.clearCheck()
            themeGroup!!.setOnCheckedChangeListener(listener1)
            themeGroup2!!.setOnCheckedChangeListener(listener2)
            themeGroup3!!.setOnCheckedChangeListener(listener3)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMarkerStyleBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStyleChecks()
        initGroupViews()
        initActions()
        setUpRadio()
    }

    private fun initActions() {
        themeGroup!!.clearCheck()
        themeGroup2!!.clearCheck()
        themeGroup3!!.clearCheck()
        themeGroup4!!.clearCheck()
        themeGroup!!.setOnCheckedChangeListener(listener1)
        themeGroup2!!.setOnCheckedChangeListener(listener2)
        themeGroup3!!.setOnCheckedChangeListener(listener3)
        themeGroup4!!.setOnCheckedChangeListener(listener4)
    }

    private fun initGroupViews() {
        themeGroup = binding!!.themeGroup
        themeGroup2 = binding!!.themeGroup2
        themeGroup3 = binding!!.themeGroup3
        themeGroup4 = binding!!.themeGroup4
    }

    private fun initStyleChecks() {
        red = binding!!.redCheck
        green = binding!!.greenCheck
        blue = binding!!.blueCheck
        yellow = binding!!.yellowCheck
        greenLight = binding!!.greenLightCheck
        blueLight = binding!!.blueLightCheck
        cyan = binding!!.cyanCheck
        purple = binding!!.purpleCheck
        amber = binding!!.amberCheck
        orange = binding!!.orangeCheck
        pink = binding!!.pinkCheck
        teal = binding!!.tealCheck
        deepPurple = binding!!.deepPurpleCheck
        deepOrange = binding!!.deepOrangeCheck
        indigo = binding!!.indigoCheck
        lime = binding!!.limeCheck
    }

    private fun setUpRadio() {
        val loaded = prefs!!.markerStyle
        if (loaded == 0) {
            red!!.isChecked = true
        } else if (loaded == 1) {
            purple!!.isChecked = true
        } else if (loaded == 2) {
            greenLight!!.isChecked = true
        } else if (loaded == 3) {
            green!!.isChecked = true
        } else if (loaded == 4) {
            blueLight!!.isChecked = true
        } else if (loaded == 5) {
            blue!!.isChecked = true
        } else if (loaded == 6) {
            yellow!!.isChecked = true
        } else if (loaded == 7) {
            orange!!.isChecked = true
        } else if (loaded == 8) {
            cyan!!.isChecked = true
        } else if (loaded == 9) {
            pink!!.isChecked = true
        } else if (loaded == 10) {
            teal!!.isChecked = true
        } else if (loaded == 11) {
            amber!!.isChecked = true
        } else if (loaded == 12) {
            deepPurple!!.isChecked = true
        } else if (loaded == 13) {
            deepOrange!!.isChecked = true
        } else if (loaded == 14) {
            lime!!.isChecked = true
        } else if (loaded == 15) {
            indigo!!.isChecked = true
        } else {
            blue!!.isChecked = true
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
        prefs!!.markerStyle = style
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.style_of_marker))
            callback!!.onFragmentSelect(this)
        }
    }
}
