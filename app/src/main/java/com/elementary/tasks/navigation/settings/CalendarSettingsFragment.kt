package com.elementary.tasks.navigation.settings

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.navigation.settings.calendar.FragmentBirthdaysColor
import com.elementary.tasks.navigation.settings.calendar.FragmentEventsImport
import com.elementary.tasks.navigation.settings.calendar.FragmentRemindersColor
import com.elementary.tasks.navigation.settings.calendar.FragmentTodayColor
import kotlinx.android.synthetic.main.fragment_calendar_settings.*

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

class CalendarSettingsFragment : BaseSettingsFragment() {

    private var mItemSelect: Int = 0

    override fun layoutRes(): Int = R.layout.fragment_calendar_settings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            callback?.onScrollUpdate(it)
        }

        initFuturePrefs()
        initRemindersPrefs()
        initFirstDayPrefs()
        eventsImportPrefs.setOnClickListener { replaceFragment(FragmentEventsImport(), getString(R.string.import_events)) }
    }

    private fun initFirstDayPrefs() {
        startDayPrefs.setOnClickListener { showFirstDayDialog() }
        showFirstDay()
    }

    private fun showFirstDay() {
        val items = arrayOf(getString(R.string.sunday), getString(R.string.monday))
        startDayPrefs.setDetailText(items[prefs.startDay])
    }

    private fun showFirstDayDialog() {
        val builder = dialogues.getDialog(context!!)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.first_day))
        val items = arrayOf(getString(R.string.sunday), getString(R.string.monday))
        val adapter = ArrayAdapter(context!!,
                android.R.layout.simple_list_item_single_choice, items)
        mItemSelect = prefs.startDay
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
            prefs.startDay = mItemSelect
            showFirstDay()
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun initRemindersColorPrefs() {
        reminderColorPrefs.setDependentView(reminderInCalendarPrefs)
        reminderColorPrefs.setOnClickListener { replaceFragment(FragmentRemindersColor(), getString(R.string.reminders_color)) }
        reminderColorPrefs.setViewResource(themeUtil.getIndicator(prefs.reminderColor))
    }

    private fun initRemindersPrefs() {
        reminderInCalendarPrefs.isChecked = prefs.isRemindersInCalendarEnabled
        reminderInCalendarPrefs.setOnClickListener { changeRemindersPrefs() }
    }

    private fun changeRemindersPrefs() {
        val isChecked = reminderInCalendarPrefs.isChecked
        reminderInCalendarPrefs.isChecked = !isChecked
        prefs.isRemindersInCalendarEnabled = !isChecked
    }

    private fun initFuturePrefs() {
        featureRemindersPrefs.isChecked = prefs.isFutureEventEnabled
        featureRemindersPrefs.setOnClickListener { changeFuturePrefs() }
    }

    private fun changeFuturePrefs() {
        val isChecked = featureRemindersPrefs.isChecked
        featureRemindersPrefs.isChecked = !isChecked
        prefs.isFutureEventEnabled = !isChecked
    }

    override fun onResume() {
        super.onResume()
        initRemindersColorPrefs()
        initTodayColorPrefs()
        initBirthdaysColorPrefs()

        callback?.onTitleChange(getString(R.string.calendar))
        callback?.onFragmentSelect(this)
    }

    private fun initBirthdaysColorPrefs() {
        selectedColorPrefs.setOnClickListener { replaceFragment(FragmentBirthdaysColor(), getString(R.string.birthdays_color)) }
        selectedColorPrefs.setViewResource(themeUtil.getIndicator(prefs.birthdayColor))
    }

    private fun initTodayColorPrefs() {
        themeColorPrefs.setOnClickListener { replaceFragment(FragmentTodayColor(), getString(R.string.today_color)) }
        themeColorPrefs.setViewResource(themeUtil.getIndicator(prefs.todayColor))
    }
}
