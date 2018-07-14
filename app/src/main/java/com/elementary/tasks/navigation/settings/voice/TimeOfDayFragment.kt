package com.elementary.tasks.navigation.settings.voice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.views.roboto.RoboTextView
import com.elementary.tasks.databinding.FragmentTimeOfDayBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

class TimeOfDayFragment : BaseSettingsFragment(), View.OnClickListener {

    private var nightTime: RoboTextView? = null
    private var eveningTime: RoboTextView? = null
    private var dayTime: RoboTextView? = null
    private var morningTime: RoboTextView? = null
    private var morningHour: Int = 0
    private var morningMinute: Int = 0
    private var dayHour: Int = 0
    private var dayMinute: Int = 0
    private var eveningHour: Int = 0
    private var eveningMinute: Int = 0
    private var nightHour: Int = 0
    private var nightMinute: Int = 0
    private var is24: Boolean = false
    private val format = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentTimeOfDayBinding.inflate(inflater, container, false)
        nightTime = binding.nightTime
        nightTime!!.setOnClickListener(this)
        eveningTime = binding.eveningTime
        eveningTime!!.setOnClickListener(this)
        dayTime = binding.dayTime
        dayTime!!.setOnClickListener(this)
        morningTime = binding.morningTime
        morningTime!!.setOnClickListener(this)

        is24 = prefs!!.is24HourFormatEnabled

        initMorningTime()
        initNoonTime()
        initEveningTime()
        initNightTime()
        return binding.root
    }

    private fun initNoonTime() {
        val noonTime = prefs!!.noonTime
        var date: Date? = null
        try {
            date = format.parse(noonTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val calendar = Calendar.getInstance()
        if (date != null) calendar.time = date
        dayHour = calendar.get(Calendar.HOUR_OF_DAY)
        dayMinute = calendar.get(Calendar.MINUTE)
        dayTime!!.text = TimeUtil.getTime(calendar.time, is24)
    }

    private fun initEveningTime() {
        val evening = prefs!!.eveningTime
        var date: Date? = null
        try {
            date = format.parse(evening)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val calendar = Calendar.getInstance()
        if (date != null) calendar.time = date
        eveningHour = calendar.get(Calendar.HOUR_OF_DAY)
        eveningMinute = calendar.get(Calendar.MINUTE)
        eveningTime!!.text = TimeUtil.getTime(calendar.time, is24)
    }

    private fun initNightTime() {
        val night = prefs!!.nightTime
        var date: Date? = null
        try {
            date = format.parse(night)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        if (date != null) calendar.time = date
        nightHour = calendar.get(Calendar.HOUR_OF_DAY)
        nightMinute = calendar.get(Calendar.MINUTE)
        nightTime!!.text = TimeUtil.getTime(calendar.time, is24)
    }

    private fun initMorningTime() {
        val morning = prefs!!.morningTime
        var date: Date? = null
        try {
            date = format.parse(morning)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        if (date != null) calendar.time = date
        morningHour = calendar.get(Calendar.HOUR_OF_DAY)
        morningMinute = calendar.get(Calendar.MINUTE)
        morningTime!!.text = TimeUtil.getTime(calendar.time, is24)
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.time))
            callback!!.onFragmentSelect(this)
        }
    }

    private fun morningDialog() {
        TimeUtil.showTimePicker(context, { view, hourOfDay, minute ->
            morningHour = hourOfDay
            morningMinute = minute
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val time = format.format(calendar.time)
            prefs!!.morningTime = time
            morningTime!!.text = TimeUtil.getTime(calendar.time, is24)
        }, morningHour, morningMinute)
    }

    private fun dayDialog() {
        TimeUtil.showTimePicker(context, { view, hourOfDay, minute ->
            dayHour = hourOfDay
            dayMinute = minute
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val time = format.format(calendar.time)
            prefs!!.noonTime = time
            dayTime!!.text = TimeUtil.getTime(calendar.time, is24)
        }, dayHour, dayMinute)
    }

    private fun nightDialog() {
        TimeUtil.showTimePicker(context, { view, hourOfDay, minute ->
            nightHour = hourOfDay
            nightMinute = minute
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val time = format.format(calendar.time)
            prefs!!.nightTime = time
            nightTime!!.text = TimeUtil.getTime(calendar.time, is24)
        }, nightHour, nightMinute)
    }

    private fun eveningDialog() {
        TimeUtil.showTimePicker(context, { view, hourOfDay, minute ->
            eveningHour = hourOfDay
            eveningMinute = minute
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val time = format.format(calendar.time)
            prefs!!.eveningTime = time
            eveningTime!!.text = TimeUtil.getTime(calendar.time, is24)
        }, eveningHour, eveningMinute)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.morningTime -> morningDialog()
            R.id.dayTime -> dayDialog()
            R.id.eveningTime -> eveningDialog()
            R.id.nightTime -> nightDialog()
        }
    }
}