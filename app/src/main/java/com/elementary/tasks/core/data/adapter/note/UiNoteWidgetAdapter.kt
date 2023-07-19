package com.elementary.tasks.core.data.adapter.note

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteWidget
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.dp2px
import com.elementary.tasks.core.utils.ui.font.FontParams
import com.elementary.tasks.core.views.NoteTextDrawable


class UiNoteWidgetAdapter(
  private val themeProvider: ThemeProvider,
  private val contextProvider: ContextProvider,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter
) {

  fun convertDp(noteWithImages: NoteWithImages, widthDp: Int, heightDp: Int): UiNoteWidget {
    val maxSize = maxOf(
      contextProvider.context.dp2px(widthDp),
      contextProvider.context.dp2px(heightDp)
    ) * 2
    return convert(
      noteWithImages = noteWithImages,
      size = maxSize
    )
  }

  fun convert(noteWithImages: NoteWithImages, size: Int): UiNoteWidget {
    val backgroundColor = themeProvider.getNoteLightColor(
      noteWithImages.getColor(),
      noteWithImages.getOpacity(),
      noteWithImages.getPalette()
    )

    val textColor = if ((noteWithImages.getOpacity().isAlmostTransparent() &&
        themeProvider.isDark) || backgroundColor.isColorDark()
    ) {
      ContextCompat.getColor(contextProvider.context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(contextProvider.context, R.color.pureBlack)
    }

    val textSize = if (noteWithImages.getFontSize() == -1) {
      FontParams.DEFAULT_FONT_SIZE
    } else {
      noteWithImages.getFontSize()
    }

    val typeface = AssetsUtil.getTypeface(contextProvider.context, noteWithImages.getStyle())!!

    val radius = contextProvider.context.dp2px(28)

    val isDarkIcon = if (noteWithImages.getOpacity().isAlmostTransparent()) {
      themeProvider.isDark
    } else {
      backgroundColor.isColorDark()
    }

    val image = noteWithImages.images.firstOrNull()?.let {
      uiNoteImagesAdapter.convert(listOf(it))
    }?.firstOrNull()?.let {
      BitmapFactory.decodeFile(it.filePath)
    }?.let { bitmap ->
      val scale = size.toFloat() / minOf(bitmap.width, bitmap.height)

      val matrix = Matrix()
      matrix.postScale(scale, scale)
      val resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
      bitmap.recycle()
      resizedBitmap
    }

    val builder = NoteTextDrawable.builder(contextProvider.context).beginConfig()
      .height(size)
      .width(size)
      .fontSize(textSize * 2)
      .textColor(textColor)
      .useFont(typeface)
    image?.also { builder.withImage(it) }

    val bitmap = builder
      .endConfig()
      .buildRoundRect(noteWithImages.getSummary(), backgroundColor, radius)
      .toBitmap(width = size, height = size)

    return UiNoteWidget(
      id = noteWithImages.getKey(),
      uniqueId = noteWithImages.note?.uniqueId ?: 1111,
      bitmap = bitmap,
      settingsIcon = ViewUtils.tintIcon(
        contextProvider.context,
        R.drawable.ic_twotone_settings_24px,
        isDarkIcon
      )?.toBitmap()
    )
  }
}
