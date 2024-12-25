package com.github.naz013.appwidgets.singlenote.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.toBitmap
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.singlenote.drawable.NoteDrawableParams
import com.github.naz013.appwidgets.singlenote.drawable.NoteTextDrawable
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.UnitsConverter
import com.github.naz013.ui.common.font.FontApi
import com.github.naz013.ui.common.isAlmostTransparent
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.ViewUtils

internal class UiNoteWidgetAdapter(
  private val themeProvider: ThemeProvider,
  private val contextProvider: ContextProvider,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter,
  private val unitsConverter: UnitsConverter,
  private val fontApi: FontApi
) {

  fun convert(
    noteWithImages: NoteWithImages,
    size: Int,
    fontSize: Float,
    @ColorInt textColor: Int,
    horizontalAlignment: NoteDrawableParams.HorizontalAlignment =
      NoteDrawableParams.HorizontalAlignment.CENTER,
    verticalAlignment: NoteDrawableParams.VerticalAlignment =
      NoteDrawableParams.VerticalAlignment.CENTER,
    margin: Float,
    @ColorInt overlayColor: Int = Color.TRANSPARENT
  ): UiNoteWidget {
    val backgroundColor = themeProvider.getNoteLightColor(
      noteWithImages.getColor(),
      noteWithImages.getOpacity(),
      noteWithImages.getPalette()
    )

    val typeface = fontApi.getTypeface(noteWithImages.getStyle())!!

    val radius = unitsConverter.dp2px(28)

    val isDarkIcon = if (noteWithImages.getOpacity().isAlmostTransparent()) {
      themeProvider.isDark
    } else {
      backgroundColor.isColorDark()
    }

    val startMillis = System.currentTimeMillis()

    val image = noteWithImages.images.firstOrNull()?.let {
      uiNoteImagesAdapter.convert(listOf(it))
    }?.firstOrNull()?.let {
      BitmapFactory.decodeFile(it.filePath)
    }?.let { bitmap ->
      val scale = size.toFloat() / minOf(bitmap.width, bitmap.height)

      val matrix = Matrix()
      matrix.postScale(scale, scale)
      val resizedBitmap =
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
      bitmap.recycle()
      resizedBitmap
    }

    Logger.d("convert: image time -> ${System.currentTimeMillis() - startMillis}")

    val params = NoteDrawableParams.roundedRectParams(
      context = contextProvider.themedContext,
      height = size,
      width = size,
      fontSize = fontSize,
      textColor = textColor,
      font = typeface,
      textAutoScale = false,
      horizontalAlignment = horizontalAlignment,
      verticalAlignment = verticalAlignment,
      margin = margin,
      backgroundImage = image,
      text = noteWithImages.getSummary(),
      color = backgroundColor,
      radius = radius,
      overlayParams = NoteDrawableParams.OverlayParams(overlayColor)
    )

    val bitmap = if (size != 0) {
      NoteTextDrawable(params).toBitmap(width = size, height = size)
    } else {
      null
    }

    Logger.d("convert: full drawable -> ${System.currentTimeMillis() - startMillis}")

    return UiNoteWidget(
      id = noteWithImages.getKey(),
      uniqueId = noteWithImages.note?.uniqueId ?: 1111,
      bitmap = bitmap,
      settingsIcon = ViewUtils.tintIcon(
        contextProvider.themedContext,
        R.drawable.ic_fluent_settings,
        isDarkIcon
      )?.toBitmap()
    )
  }
}
