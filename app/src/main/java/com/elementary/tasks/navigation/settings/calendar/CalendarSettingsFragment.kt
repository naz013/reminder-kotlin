package com.elementary.tasks.navigation.settings.calendar

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import kotlinx.android.synthetic.main.fragment_settings_calendar.*
import kotlinx.android.synthetic.main.view_color_slider.view.*

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

    override fun layoutRes(): Int = R.layout.fragment_settings_calendar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtils.listenScrollableView(scrollView) {
            callback?.onScrollUpdate(it)
        }

        initFuturePrefs()
        initRemindersPrefs()
        initFirstDayPrefs()
        eventsImportPrefs.setOnClickListener { callback?.openFragment(FragmentEventsImport(), getString(R.string.import_events)) }

        reminderColorPrefs.setDependentView(reminderInCalendarPrefs)
        reminderColorPrefs.setOnClickListener {
            showColorPopup(prefs.reminderColor, getString(R.string.reminders_color)) { color ->
                prefs.reminderColor = color
                initRemindersColorPrefs()
            }
        }
        initRemindersColorPrefs()

        themeColorPrefs.setOnClickListener {
            showColorPopup(prefs.todayColor, getString(R.string.today_color)) { color ->
                prefs.todayColor = color
                initTodayColorPrefs()
            }
        }
        initTodayColorPrefs()

        selectedColorPrefs.setOnClickListener {
            showColorPopup(prefs.birthdayColor, getString(R.string.birthdays_color)) { color ->
                prefs.birthdayColor = color
                initBirthdaysColorPrefs()
            }
        }
        initBirthdaysColorPrefs()
    }

    private fun showColorPopup(current: Int, title: String, onSave: (Int) -> Unit) {
        val builder = dialogues.getDialog(context!!)
        val layout = layoutInflater.inflate(R.layout.view_color_slider, null, false)
        builder.setView(layout)
        val slider = layout.colorSlider
        slider.setColors(themeUtil.colorsForSlider())
        slider.setSelection(current)
        builder.setTitle(title)
        builder.setPositiveButton(R.string.save) { d, _ ->
            onSave.invoke(slider.selectedItem)
            d.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { d, _ ->
            d.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity!!)
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
        reminderColorPrefs.setViewColor(themeUtil.colorReminderCalendar())
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

    override fun getTitle(): String = getString(R.string.calendar)

    private fun initBirthdaysColorPrefs() {
        selectedColorPrefs.setViewColor(themeUtil.colorBirthdayCalendar())
    }

    private fun initTodayColorPrefs() {
        themeColorPrefs.setViewColor(themeUtil.colorCurrentCalendar())
    }
}
