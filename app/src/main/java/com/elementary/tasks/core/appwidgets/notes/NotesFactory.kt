package com.elementary.tasks.core.appwidgets.notes

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.text.SpannableString
import android.text.style.TypefaceSpan
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.appwidgets.AppWidgetActionActivity
import com.elementary.tasks.core.appwidgets.Direction
import com.elementary.tasks.core.appwidgets.WidgetIntentProtocol
import com.elementary.tasks.core.data.invokeSuspend
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.github.naz013.domain.font.FontParams
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.repository.NoteRepository

class NotesFactory(
  private val context: Context,
  private val noteRepository: NoteRepository,
  private val themeProvider: ThemeProvider
) : RemoteViewsService.RemoteViewsFactory {

  private val notes = ArrayList<NoteWithImages>()
  private var defaultTextSize = FontParams.DEFAULT_FONT_SIZE

  override fun onCreate() {
    notes.clear()
  }

  override fun onDataSetChanged() {
    notes.clear()
    notes.addAll(invokeSuspend { noteRepository.getAll() })
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
    val textSize = if (note.getFontSize() == -1) {
      defaultTextSize
    } else {
      note.getFontSize()
    }
    rv.setTextViewTextSize(R.id.note, TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
    rv.setInt(
      R.id.noteBackground,
      "setBackgroundColor",
      themeProvider.getNoteLightColor(note.getColor(), note.getOpacity(), note.getPalette())
    )
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

    val data = WidgetIntentProtocol(
      mapOf<String, Any?>(
        Pair(Constants.INTENT_ID, note.getKey())
      )
    )

    val fillInIntent = Intent().apply {
      putExtra(AppWidgetActionActivity.DIRECTION, Direction.NOTE)
      putExtra(AppWidgetActionActivity.DATA, data)
    }
    rv.setOnClickFillInIntent(R.id.note, fillInIntent)
    rv.setOnClickFillInIntent(R.id.noteImage, fillInIntent)
    rv.setOnClickFillInIntent(R.id.noteBackground, fillInIntent)
    return rv
  }

  private fun getNoteText(note: NoteWithImages): SpannableString {
    val spannableString = SpannableString(note.getSummary())

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      AssetsUtil.getTypeface(context, note.getStyle())?.also {
        spannableString.setSpan(
          TypefaceSpan(it),
          0,
          spannableString.length,
          SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
        )
      }
    }
    return spannableString
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
