package com.elementary.tasks.intro

import android.content.Context
import androidx.annotation.RawRes
import com.elementary.tasks.R

internal object ItemFactory {

    fun getItem(context: Context, position: Int): IntroItem? {
        when (position) {
            0 -> return createItem(context.getString(R.string.reminder), context.getString(R.string.reminder_descr), R.raw.intro_tasks)
            1 -> return createItem(context.getString(R.string.notes_support), context.getString(R.string.notes_descr), R.raw.intro_notes)
            2 -> return createItem(context.getString(R.string.location_events), context.getString(R.string.location_descr), R.raw.intro_map)
            3 -> return createItem(context.getString(R.string.sync), context.getString(R.string.sync_descr), R.raw.intro_backup)
        }
        return null
    }

    private fun createItem(string: String, description: String, @RawRes icon: Int): IntroItem {
        return IntroItem(string, description, icon)
    }
}
