package com.elementary.tasks.core.appwidgets.singlenote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.drawable.toBitmap
import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.note.UiNoteImagesAdapter
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.ui.note.UiNoteWidget
import com.elementary.tasks.core.os.ColorProvider
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.os.UnitsConverter
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.io.AssetsUtil
import com.elementary.tasks.core.utils.isAlmostTransparent
import com.elementary.tasks.core.utils.isColorDark
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.views.drawable.NoteDrawableParams
import com.elementary.tasks.core.views.drawable.NoteTextDrawable
import timber.log.Timber

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
    horizontalAlignment: NoteDrawableParams.HorizontalAlignment =
      NoteDrawableParams.HorizontalAlignment.CENTER,
    verticalAlignment: NoteDrawableParams.VerticalAlignment =
      NoteDrawableParams.VerticalAlignment.CENTER,
    marginDp: Int
  ): UiNoteWidget {
    val maxSize = unitsConverter.dp2px(sizeDp)

    val backgroundColor = themeProvider.getNoteLightColor(
      noteWithImages.getColor(),
      noteWithImages.getOpacity(),
      noteWithImages.getPalette()
    )

    val isDarkBg = (noteWithImages.getOpacity().isAlmostTransparent() && themeProvider.isDark) ||
      backgroundColor.isColorDark()
    val textColor = if (isDarkBg) {
      colorProvider.getColor(R.color.pureWhite)
    } else {
      colorProvider.getColor(R.color.pureBlack)
    }

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

    Timber.d("convert: image time -> ${System.currentTimeMillis() - startMillis}")

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
      radius = radius
    )

    val bitmap = NoteTextDrawable(params).toBitmap(
      width = maxSize.toInt(),
      height = maxSize.toInt()
    )

    Timber.d("convert: full drawable -> ${System.currentTimeMillis() - startMillis}")

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
