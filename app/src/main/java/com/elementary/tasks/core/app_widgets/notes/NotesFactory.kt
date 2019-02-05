package com.elementary.tasks.core.app_widgets.notes

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.NoteWithImages
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

    private val notes = ArrayList<NoteWithImages>()
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

    private fun getItem(position: Int): NoteWithImages? {
        return try {
            notes[position]
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    override fun getViewAt(i: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.list_item_widget_note)
        val note = getItem(i)
        if (note == null) {
            rv.setTextViewText(R.id.note, mContext.getString(R.string.failed_to_load))
            return rv
        }
        rv.setInt(R.id.noteBackground, "setBackgroundColor",
                themeUtil.getNoteLightColor(note.getColor(), note.getOpacity(), note.getPalette()))
        if (themeUtil.isAlmostTransparent(note.getOpacity())) {
            rv.setTextColor(R.id.note, ContextCompat.getColor(mContext, R.color.pureWhite))
        } else {
            rv.setTextColor(R.id.note, ContextCompat.getColor(mContext, R.color.pureBlack))
        }

        if (note.images.isNotEmpty()) {
            val image = note.images[0]
            val imageData = image.image
            if (imageData != null) {
                val photo = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                if (photo != null) {
                    rv.setImageViewBitmap(R.id.noteImage, photo)
                    rv.setViewVisibility(R.id.noteImage, View.VISIBLE)
                } else {
                    rv.setViewVisibility(R.id.noteImage, View.GONE)
                }
            } else {
                rv.setViewVisibility(R.id.noteImage, View.GONE)
            }
        } else {
            rv.setViewVisibility(R.id.noteImage, View.GONE)
        }
        rv.setTextViewText(R.id.note, note.getSummary())
        val fillInIntent = Intent()
        fillInIntent.putExtra(Constants.INTENT_ID, note.getKey())
        rv.setOnClickFillInIntent(R.id.note, fillInIntent)
        rv.setOnClickFillInIntent(R.id.noteImage, fillInIntent)
        rv.setOnClickFillInIntent(R.id.noteBackground, fillInIntent)
        return rv
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