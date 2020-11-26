package com.elementary.tasks.core.app_widgets.notes

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.isAlmostTransparent
import java.util.*

class NotesFactory(
  private val context: Context,
  private val appDb: AppDb,
  private val themeProvider: ThemeProvider
) : RemoteViewsService.RemoteViewsFactory {

  private val notes = ArrayList<NoteWithImages>()

  override fun onCreate() {
    notes.clear()
  }

  override fun onDataSetChanged() {
    notes.clear()
    notes.addAll(appDb.notesDao().all())
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
    val rv = RemoteViews(context.packageName, R.layout.list_item_widget_note)
    val note = getItem(i)
    if (note == null) {
      rv.setTextViewText(R.id.note, context.getString(R.string.failed_to_load))
      return rv
    }
    rv.setInt(R.id.noteBackground, "setBackgroundColor",
      themeProvider.getNoteLightColor(note.getColor(), note.getOpacity(), note.getPalette()))
    if (note.getOpacity().isAlmostTransparent()) {
      rv.setTextColor(R.id.note, ContextCompat.getColor(context, R.color.pureWhite))
    } else {
      rv.setTextColor(R.id.note, ContextCompat.getColor(context, R.color.pureBlack))
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