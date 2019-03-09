package com.elementary.tasks.navigation.settings.calendar

import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsCalendarBinding
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
class CalendarSettingsFragment : BaseSettingsFragment<FragmentSettingsCalendarBinding>() {

    private var mItemSelect: Int = 0

    override fun layoutRes(): Int = R.layout.fragment_settings_calendar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(binding.scrollView) {
            setScroll(it)
        }

        initFuturePrefs()
        initRemindersPrefs()
        initFirstDayPrefs()
        binding.eventsImportPrefs.setOnClickListener { callback?.openFragment(FragmentEventsImport(), getString(R.string.import_events)) }

        binding.reminderColorPrefs.setDependentView(binding.reminderInCalendarPrefs)
        binding.reminderColorPrefs.setOnClickListener {
            showColorPopup(prefs.reminderColor, getString(R.string.reminders_color)) { color ->
                prefs.reminderColor = color
                initRemindersColorPrefs()
            }
        }
        initRemindersColorPrefs()

        binding.themeColorPrefs.setOnClickListener {
            showColorPopup(prefs.todayColor, getString(R.string.today_color)) { color ->
                prefs.todayColor = color
                initTodayColorPrefs()
            }
        }
        initTodayColorPrefs()

        binding.selectedColorPrefs.setOnClickListener {
            showColorPopup(prefs.birthdayColor, getString(R.string.birthdays_color)) { color ->
                prefs.birthdayColor = color
                initBirthdaysColorPrefs()
            }
        }
        initBirthdaysColorPrefs()
    }

    private fun showColorPopup(current: Int, title: String, onSave: (Int) -> Unit) {
        withActivity { act ->
            dialogues.showColorDialog(act, current, title, themeUtil.colorsForSlider()) {
                onSave.invoke(it)
            }
        }
    }

    private fun initFirstDayPrefs() {
        binding.startDayPrefs.setOnClickListener { showFirstDayDialog() }
        showFirstDay()
    }

    private fun showFirstDay() {
        val items = arrayOf(getString(R.string.sunday), getString(R.string.monday))
        binding.startDayPrefs.setDetailText(items[prefs.startDay])
    }

    private fun showFirstDayDialog() {
        withContext {
            val builder = dialogues.getMaterialDialog(it)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.first_day))
            val items = arrayOf(getString(R.string.sunday), getString(R.string.monday))
            mItemSelect = prefs.startDay
            builder.setSingleChoiceItems(items, mItemSelect) { _, which -> mItemSelect = which }
            builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
                prefs.startDay = mItemSelect
                showFirstDay()
                dialogInterface.dismiss()
            }
            builder.create().show()
        }
    }

    private fun initRemindersColorPrefs() {
        binding.reminderColorPrefs.setViewColor(themeUtil.colorReminderCalendar())
    }

    private fun initRemindersPrefs() {
        binding.reminderInCalendarPrefs.isChecked = prefs.isRemindersInCalendarEnabled
        binding.reminderInCalendarPrefs.setOnClickListener { changeRemindersPrefs() }
    }

    private fun changeRemindersPrefs() {
        val isChecked = binding.reminderInCalendarPrefs.isChecked
        binding.reminderInCalendarPrefs.isChecked = !isChecked
        prefs.isRemindersInCalendarEnabled = !isChecked
    }

    private fun initFuturePrefs() {
        binding.featureRemindersPrefs.isChecked = prefs.isFutureEventEnabled
        binding.featureRemindersPrefs.setOnClickListener { changeFuturePrefs() }
    }

    private fun changeFuturePrefs() {
        val isChecked = binding.featureRemindersPrefs.isChecked
        binding.featureRemindersPrefs.isChecked = !isChecked
        prefs.isFutureEventEnabled = !isChecked
    }

    override fun getTitle(): String = getString(R.string.calendar)

    private fun initBirthdaysColorPrefs() {
        binding.selectedColorPrefs.setViewColor(themeUtil.colorBirthdayCalendar())
    }

    private fun initTodayColorPrefs() {
        binding.themeColorPrefs.setViewColor(themeUtil.colorCurrentCalendar())
    }
}
