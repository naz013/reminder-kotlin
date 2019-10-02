package com.elementary.tasks.reminder.create.selector

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Module

object OptionsFactory {

    const val BY_DATE = "by_date"
    const val BY_DATE_APP = "by_date_app"
    const val BY_DATE_SHOP = "by_date_shop"
    const val BY_DATE_SKYPE = "by_date_skype"
    const val BY_DATE_EMAIL = "by_date_email"
    const val BY_TIMER = "by_timer"
    const val BY_WEEK = "by_week"
    const val BY_MONTH = "by_month"
    const val BY_YEAR = "by_year"
    const val BY_LOCATION = "by_location"
    const val BY_PLACE = "by_place"

    fun createList(context: Context, array: Array<String>, addExtraKey: String, pointer: String = ""): List<Option> {
        if (array.isEmpty()) return createList(context, all(), addExtraKey, pointer)
        val options = mutableListOf<Option>()
        for (key in array) {
            if (!isForPro(key)) {
                if (!isLocation(key) || Module.hasLocation(context)) {
                    options.add(create(context, key))
                }
            }
        }
        if (addExtraKey.isNotEmpty() && !isForPro(addExtraKey) && !array.contains(addExtraKey)) {
            options.add(create(context, addExtraKey).apply { this.isSelected = true })
        } else {
            if (pointer.isEmpty()) {
                options[0].isSelected = true
            } else {
                for (o in options) {
                    if (o.key == pointer) {
                        o.isSelected = true
                    }
                }
            }
        }
        return options
    }

    fun keyFromType(type: Int): String {
        return when {
            Reminder.isSame(type, Reminder.BY_DATE_SHOP) -> BY_DATE_SHOP
            Reminder.isSame(type, Reminder.BY_DATE_EMAIL) -> BY_DATE_EMAIL
            Reminder.isSame(type, Reminder.BY_DATE_APP) || Reminder.isSame(type, Reminder.BY_DATE_LINK) -> BY_DATE_APP
            Reminder.isBase(type, Reminder.BY_SKYPE) -> BY_DATE_SKYPE
            Reminder.isBase(type, Reminder.BY_DATE) -> BY_DATE
            Reminder.isBase(type, Reminder.BY_TIME) -> BY_TIMER
            Reminder.isBase(type, Reminder.BY_WEEK) -> BY_WEEK
            Reminder.isBase(type, Reminder.BY_MONTH) -> BY_MONTH
            Reminder.isBase(type, Reminder.BY_DAY_OF_YEAR) -> BY_YEAR
            Reminder.isBase(type, Reminder.BY_LOCATION) || Reminder.isBase(type, Reminder.BY_LOCATION) -> BY_LOCATION
            Reminder.isBase(type, Reminder.BY_PLACES) -> BY_PLACE
            else -> BY_DATE
        }
    }

    private fun all(): Array<String> {
        return arrayOf(
                BY_DATE, BY_TIMER, BY_WEEK, BY_DATE_EMAIL, BY_DATE_SKYPE, BY_DATE_APP,
                BY_MONTH, BY_YEAR, BY_DATE_SHOP, BY_LOCATION, BY_PLACE
        )
    }

    fun create(context: Context, key: String): Option {
        return when (key) {
            BY_DATE -> Option(key, R.drawable.ic_twotone_today_24px, context.getString(R.string.by_date))
            BY_TIMER -> Option(key, R.drawable.ic_twotone_access_time_24px, context.getString(R.string.timer))
            BY_WEEK -> Option(key, R.drawable.ic_twotone_alarm_24px, context.getString(R.string.alarm))
            BY_DATE_EMAIL -> Option(key, R.drawable.ic_twotone_local_post_office_24px, context.getString(R.string.e_mail))
            BY_DATE_SKYPE -> Option(key, R.drawable.ic_skype, context.getString(R.string.skype))
            BY_DATE_APP -> Option(key, R.drawable.ic_exit_to_app_24px, context.getString(R.string.launch_application))
            BY_MONTH -> Option(key, R.drawable.ic_twotone_today_24px, context.getString(R.string.day_of_month))
            BY_YEAR -> Option(key, R.drawable.ic_twotone_cake_24px, context.getString(R.string.yearly))
            BY_DATE_SHOP -> Option(key, R.drawable.ic_twotone_shopping_cart_24px, context.getString(R.string.shopping_list))
            BY_LOCATION -> Option(key, R.drawable.ic_twotone_map_24px, context.getString(R.string.location))
            BY_PLACE -> Option(key, R.drawable.ic_twotone_place_24px, context.getString(R.string.places))
            else -> Option(key, R.drawable.ic_twotone_today_24px, context.getString(R.string.by_date))
        }
    }

    private fun isForPro(key: String) = !Module.isPro && key == BY_PLACE

    private fun isLocation(key: String) = key == BY_PLACE || key == BY_LOCATION
}