package com.elementary.tasks.reminder.createEdit.fragments

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.SuperUtil
import java.util.*

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
abstract class RadiusTypeFragment : TypeFragment() {

    protected var radius = prefs.radius

    protected fun showRadiusPickerDialog() {
        dialogues.showRadiusDialog(context!!, radius, object : Dialogues.OnValueSelectedListener<Int> {
            override fun onSelected(t: Int) {
                radius = t - 1
                recreateMarker()
            }

            override fun getTitle(t: Int): String {
                return getTitleString(t)
            }
        })
    }

    private fun getTitleString(progress: Int): String {
        return if (progress == 0) {
            getString(R.string.default_string)
        } else {
            String.format(Locale.getDefault(), getString(R.string.radius_x_meters), (progress - 1).toString())
        }
    }

    protected abstract fun recreateMarker()

    override fun prepare(): Reminder? {
        if (!SuperUtil.checkLocationEnable(context!!)) {
            SuperUtil.showLocationAlert(context!!, reminderInterface!!)
            return null
        }
        return Reminder()
    }
}
