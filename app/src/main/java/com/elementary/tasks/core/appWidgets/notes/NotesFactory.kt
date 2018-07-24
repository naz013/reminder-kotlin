package com.elementary.tasks.core.appWidgets.notes

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeUtil
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2015 Nazar Suhovich
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
class NotesFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {

    private val notes = ArrayList<Note>()
    @Inject lateinit var themeUtil: ThemeUtil

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate() {
        notes.clear()
    }

    override fun onDataSetChanged() {
        notes.clear()
        notes.addAll(AppDb.getAppDatabase(mContext).notesDao().all())
    }

    override fun onDestroy() {
        notes.clear()
    }

    override fun getCount(): Int {
        return notes.size
    }

    private fun getItem(position: Int): Note? {
        return try {
            notes[position]
        } catch (e: IndexOutOfBoundsException) {
            null
        }

    }

    override fun getViewAt(i: Int): RemoteViews {
        val rView = RemoteViews(mContext.packageName, R.layout.list_item_note_widget)
        val note = getItem(i)
        if (note == null) {
            rView.setTextViewText(R.id.note, mContext.getString(R.string.failed_to_load))
            return rView
        }
        rView.setInt(R.id.noteBackground, "setBackgroundColor", themeUtil.getNoteLightColor(note.color))

        if (note.images.isNotEmpty()) {
            val image = note.images[0]
            val photo = BitmapFactory.decodeByteArray(image.image, 0, image.image!!.size)
            if (photo != null) {
                rView.setImageViewBitmap(R.id.noteImage, photo)
                rView.setViewVisibility(R.id.noteImage, View.VISIBLE)
            } else {
                rView.setViewVisibility(R.id.noteImage, View.GONE)
            }
        } else {
            rView.setViewVisibility(R.id.noteImage, View.GONE)
        }
        rView.setTextViewText(R.id.note, note.summary)
        val fillInIntent = Intent()
        fillInIntent.putExtra(Constants.INTENT_ID, note.key)
        rView.setOnClickFillInIntent(R.id.note, fillInIntent)
        rView.setOnClickFillInIntent(R.id.noteImage, fillInIntent)
        rView.setOnClickFillInIntent(R.id.noteBackground, fillInIntent)
        return rView
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }
}