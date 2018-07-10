package com.elementary.tasks.intro

import android.content.Context
import androidx.annotation.DrawableRes

import com.elementary.tasks.R

/**
 * Copyright 2017 Nazar Suhovich
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

internal object ItemFactory {

    fun getItem(context: Context, position: Int): IntroItem? {
        when (position) {
            0 -> return createItem(context.getString(R.string.reminder), context.getString(R.string.reminder_descr), R.drawable.ic_bell_illustration)
            1 -> return createItem(context.getString(R.string.notes_support), context.getString(R.string.notes_descr), R.drawable.ic_note_ill)
            2 -> return createItem(context.getString(R.string.google_integration), context.getString(R.string.google_descr), R.drawable.ic_search_ill)
            3 -> return createItem(context.getString(R.string.location_events), context.getString(R.string.location_descr), R.drawable.ic_map_ill)
            4 -> return createItem(context.getString(R.string.sync), context.getString(R.string.sync_descr), R.drawable.ic_drive_ill, R.drawable.ic_dropbox_ill)
        }
        return null
    }

    private fun createItem(string: String, description: String, @DrawableRes vararg icons: Int): IntroItem {
        return IntroItem(string, description, *icons)
    }
}
