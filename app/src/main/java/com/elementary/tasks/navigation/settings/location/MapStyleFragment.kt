package com.elementary.tasks.navigation.settings.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.views.roboto.RoboRadioButton
import com.elementary.tasks.databinding.FragmentSettingMapStyleBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

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

    private var binding: FragmentSettingMapStyleBinding? = null

    private val selection: Int
        get() {
            if (binding!!.styleAuto.isChecked) {
                return 6
            } else if (binding!!.styleDay.isChecked) {
                return 0
            } else if (binding!!.styleRetro.isChecked) {
                return 1
            } else if (binding!!.styleSilver.isChecked) {
                return 2
            } else if (binding!!.styleNight.isChecked) {
                return 3
            } else if (binding!!.styleDark.isChecked) {
                return 4
            } else if (binding!!.styleAubergine.isChecked) {
                return 5
            }
            return 0
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingMapStyleBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.styleDay.setOnClickListener(OnClickListener { this.invoke(it) })
        binding!!.styleAubergine.setOnClickListener(OnClickListener { this.invoke(it) })
        binding!!.styleAuto.setOnClickListener(OnClickListener { this.invoke(it) })
        binding!!.styleDark.setOnClickListener(OnClickListener { this.invoke(it) })
        binding!!.styleNight.setOnClickListener(OnClickListener { this.invoke(it) })
        binding!!.styleRetro.setOnClickListener(OnClickListener { this.invoke(it) })
        binding!!.styleSilver.setOnClickListener(OnClickListener { this.invoke(it) })

        binding!!.styleDay.callOnClick()

        selectCurrent(Prefs.getInstance(context).mapStyle)
    }

    private fun selectCurrent(mapStyle: Int) {
        when (mapStyle) {
            0 -> binding!!.styleDay.callOnClick()
            1 -> binding!!.styleRetro.callOnClick()
            2 -> binding!!.styleSilver.callOnClick()
            3 -> binding!!.styleNight.callOnClick()
            4 -> binding!!.styleDark.callOnClick()
            5 -> binding!!.styleAubergine.callOnClick()
            6 -> binding!!.styleAuto.callOnClick()
        }
    }

    private operator fun invoke(v: View) {
        clearChecks()
        if (v is RoboRadioButton) {
            v.isChecked = true
        }
    }

    private fun clearChecks() {
        binding!!.styleDay.isChecked = false
        binding!!.styleAubergine.isChecked = false
        binding!!.styleAuto.isChecked = false
        binding!!.styleDark.isChecked = false
        binding!!.styleNight.isChecked = false
        binding!!.styleRetro.isChecked = false
        binding!!.styleSilver.isChecked = false
    }

    override fun onDestroy() {
        super.onDestroy()
        Prefs.getInstance(context).mapStyle = selection
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.map_style))
            callback!!.onFragmentSelect(this)
        }
    }

    companion object {

        fun newInstance(): MapStyleFragment {
            return MapStyleFragment()
        }
    }
}
