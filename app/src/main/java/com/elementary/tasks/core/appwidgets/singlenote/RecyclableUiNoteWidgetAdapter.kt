package com.elementary.tasks.core.appwidgets.singlenote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.toBitmap
import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.note.UiNoteImagesAdapter
import com.github.naz013.domain.note.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteWidget
import com.elementary.tasks.core.os.ColorProvider
import com.github.naz013.feature.common.android.ContextProvider
import com.elementary.tasks.core.os.UnitsConverter
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.github.naz013.feature.common.android.isAlmostTransparent
import com.github.naz013.feature.common.android.isColorDark
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.views.drawable.NoteDrawableParams
import com.elementary.tasks.core.views.drawable.NoteTextDrawable
import com.github.naz013.logging.Logger

class RecyclableUiNoteWidgetAdapter(
  private val themeProvider: ThemeProvider,
  private val contextProvider: ContextProvider,
  private val uiNoteImagesAdapter: UiNoteImagesAdapter,
  private val unitsConverter: UnitsConverter,
  private val colorProvider: ColorProvider
) {

  private var cachedImage: Bitmap? = null
  private var cachedImageId: String? = null

  fun clear() {
    cachedImage = null
    cachedImageId = null
  }

  fun convertDp(
    noteWithImages: NoteWithImages,
    sizeDp: Int,
    fontSize: Float,
    @ColorInt textColor: Int,
    horizontalAlignment: NoteDrawableParams.HorizontalAlignment =
      NoteDrawableParams.HorizontalAlignment.CENTER,
    verticalAlignment: NoteDrawableParams.VerticalAlignment =
      NoteDrawableParams.VerticalAlignment.CENTER,
    marginDp: Int,
    @ColorInt overlayColor: Int = Color.TRANSPARENT
  ): UiNoteWidget {
    val maxSize = unitsConverter.dp2px(sizeDp)

    val backgroundColor = themeProvider.getNoteLightColor(
      noteWithImages.getColor(),
      noteWithImages.getOpacity(),
      noteWithImages.getPalette()
    )

    val typeface = AssetsUtil.getTypeface(
      contextProvider.themedContext,
      noteWithImages.getStyle()
    )!!

    val radius = unitsConverter.dp2px(28)

    val isDarkIcon = if (noteWithImages.getOpacity().isAlmostTransparent()) {
      themeProvider.isDark
    } else {
      backgroundColor.isColorDark()
    }

    val startMillis = System.currentTimeMillis()

    val image = if (noteWithImages.getKey() == cachedImageId && cachedImage != null) {
      cachedImage
    } else {
      noteWithImages.images.firstOrNull()?.let {
        uiNoteImagesAdapter.convert(listOf(it))
      }?.firstOrNull()?.let {
        BitmapFactory.decodeFile(it.filePath)
      }?.let { bitmap ->
        val scale = maxSize / minOf(bitmap.width, bitmap.height)

        val matrix = Matrix()
        matrix.postScale(scale, scale)
        val resizedBitmap =
          Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
        bitmap.recycle()
        resizedBitmap
      }?.also {
        cachedImage = it
        cachedImageId = noteWithImages.getKey()
      }
    }

    Logger.d("convert: image time -> ${System.currentTimeMillis() - startMillis}")

    val params = NoteDrawableParams.roundedRectParams(
      context = contextProvider.themedContext,
      height = maxSize.toInt(),
      width = maxSize.toInt(),
      fontSize = fontSize,
      textColor = textColor,
      font = typeface,
      textAutoScale = false,
      horizontalAlignment = horizontalAlignment,
      verticalAlignment = verticalAlignment,
      margin = unitsConverter.dp2px(marginDp),
      backgroundImage = image,
      text = noteWithImages.getSummary(),
      color = backgroundColor,
      radius = radius,
      overlayParams = NoteDrawableParams.OverlayParams(overlayColor)
    )

    val bitmap = NoteTextDrawable(params).toBitmap(
      width = maxSize.toInt(),
      height = maxSize.toInt()
    )

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
