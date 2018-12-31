package com.elementary.tasks.navigation.settings.general.home

import android.content.Context
import androidx.annotation.IdRes
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.GTasks

object PageIdentifier {

    fun availablePages(context: Context): List<HomePage> {
        val list = mutableListOf<HomePage>()
        list.add(HomePage(context.getString(R.string.tasks), 0, "home"))
        list.add(HomePage(context.getString(R.string.notes), 1, "notes"))
        list.add(HomePage(context.getString(R.string.birthdays), 2, "birthdays"))
        list.add(HomePage(context.getString(R.string.calendar), 3, "calendar"))
        list.add(HomePage(context.getString(R.string.events), 4, "day_view"))
        if (hasGoogleTasks(context)) {
            list.add(HomePage(context.getString(R.string.google_tasks), 5, "google_tasks"))
        }
        return list
    }

    @IdRes
    fun menuId(context: Context, index: Int): Int {
        return when (index) {
            0 -> R.id.nav_current
            1 -> R.id.nav_notes
            2 -> R.id.nav_birthdays
            3 -> R.id.nav_calendar
            4 -> R.id.nav_day_view
            5 -> {
                if (hasGoogleTasks(context)) R.id.nav_tasks
                else R.id.nav_current
            }
            else -> R.id.nav_current
        }
    }

    @IdRes
    fun menuId(context: Context, key: String): Int {
        return when (key) {
            "home" -> R.id.nav_current
            "notes" -> R.id.nav_notes
            "birthdays" -> R.id.nav_birthdays
            "calendar" -> R.id.nav_calendar
            "day_view" -> R.id.nav_day_view
            "google_tasks" -> {
                if (hasGoogleTasks(context)) R.id.nav_tasks
                else R.id.nav_current
            }
            else -> R.id.nav_current
        }
    }

    fun index(context: Context, key: String): Int {
        return when (key) {
            "home" -> 0
            "notes" -> 1
            "birthdays" -> 2
            "calendar" -> 3
            "day_view" -> 4
            "google_tasks" -> {
                if (hasGoogleTasks(context)) 5
                else 0
            }
            else -> 0
        }
    }

    fun name(context: Context, key: String): String {
        return when (key) {
            "home" -> context.getString(R.string.tasks)
            "notes" -> context.getString(R.string.notes)
            "birthdays" -> context.getString(R.string.birthdays)
            "calendar" -> context.getString(R.string.calendar)
            "day_view" -> context.getString(R.string.events)
            "google_tasks" -> {
                if (hasGoogleTasks(context)) context.getString(R.string.google_tasks)
                else context.getString(R.string.tasks)
            }
            else -> context.getString(R.string.tasks)
        }
    }

    private fun hasGoogleTasks(context: Context): Boolean {
        return GTasks.getInstance(context)?.isLogged ?: false
    }
}