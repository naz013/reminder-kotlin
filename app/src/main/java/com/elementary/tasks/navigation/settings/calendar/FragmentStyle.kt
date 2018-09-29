package com.elementary.tasks.navigation.settings.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.views.ColorPickerView
import com.elementary.tasks.navigation.settings.BaseCalendarFragment
import kotlinx.android.synthetic.main.fragment_settings_calendar_style.*

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
abstract class FragmentStyle : BaseCalendarFragment(), ColorPickerView.OnColorListener {

    protected abstract val selectedColor: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings_calendar_style, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initColorPicker()
    }

    private fun initColorPicker() {
        pickerView.setListener(this)
        pickerView.setSelectedColor(selectedColor)
    }

    private fun saveColor(code: Int) {
        saveToPrefs(code)
        updatesHelper.updateCalendarWidget()
    }

    protected abstract fun saveToPrefs(code: Int)

    override fun onColorSelect(code: Int) {
        saveColor(code)
    }
}
