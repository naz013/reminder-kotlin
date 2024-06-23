package com.elementary.tasks.core.views.drawable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import androidx.annotation.ColorInt
import com.elementary.tasks.core.os.dp2px

data class NoteDrawableParams(
  val context: Context,
  val text: String = "",
  val color: Int = Color.GRAY,
  val borderThickness: Int = 0,
  val width: Int = 0,
  val height: Int = 0,
  val font: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL),
  val shape: RectShape = RectShape(),
  val textColor: Int = Color.WHITE,
  val fontSize: Float = 0f,
  val margin: Float = context.dp2px(16).toFloat(),
  val radius: Float = 0.toFloat(),
  val backgroundImage: Bitmap? = null,
  val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.CENTER,
  val verticalAlignment: VerticalAlignment = VerticalAlignment.CENTER,
  val textAutoScale: Boolean = true,
  val overlayParams: OverlayParams = OverlayParams(Color.TRANSPARENT)
) {

  data class OverlayParams(
    @ColorInt val color: Int = Color.GRAY
  )

  enum class HorizontalAlignment {
    LEFT, CENTER, RIGHT
  }

  enum class VerticalAlignment {
    TOP, CENTER, BOTTOM
  }

  companion object {

    fun rectParams(
      context: Context,
      text: String = "",
      color: Int = Color.GRAY,
      borderThickness: Int = 0,
      width: Int = 0,
      height: Int = 0,
      font: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL),
      @ColorInt textColor: Int = Color.WHITE,
      fontSize: Float = 0f,
      margin: Float = context.dp2px(16).toFloat(),
      radius: Float = 0.toFloat(),
      backgroundImage: Bitmap? = null,
      horizontalAlignment: HorizontalAlignment = HorizontalAlignment.CENTER,
      verticalAlignment: VerticalAlignment = VerticalAlignment.CENTER,
      textAutoScale: Boolean = true,
      overlayParams: OverlayParams = OverlayParams(Color.TRANSPARENT)
    ): NoteDrawableParams {
      return NoteDrawableParams(
        context = context,
        text = text,
        color = color,
        borderThickness = borderThickness,
        width = width,
        height = height,
        font = font,
        shape = RectShape(),
        textColor = textColor,
        fontSize = fontSize,
        margin = margin,
        radius = radius,
        backgroundImage = backgroundImage,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        textAutoScale = textAutoScale,
        overlayParams = overlayParams
      )
    }

    fun roundParams(
      context: Context,
      text: String = "",
      color: Int = Color.GRAY,
      borderThickness: Int = 0,
      width: Int = 0,
      height: Int = 0,
      font: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL),
      @ColorInt textColor: Int = Color.WHITE,
      fontSize: Float = 0f,
      margin: Float = context.dp2px(16).toFloat(),
      radius: Float = 0.toFloat(),
      backgroundImage: Bitmap? = null,
      horizontalAlignment: HorizontalAlignment = HorizontalAlignment.CENTER,
      verticalAlignment: VerticalAlignment = VerticalAlignment.CENTER,
      textAutoScale: Boolean = true,
      overlayParams: OverlayParams = OverlayParams(Color.TRANSPARENT)
    ): NoteDrawableParams {
      return NoteDrawableParams(
        context = context,
        text = text,
        color = color,
        borderThickness = borderThickness,
        width = width,
        height = height,
        font = font,
        shape = OvalShape(),
        textColor = textColor,
        fontSize = fontSize,
        margin = margin,
        radius = radius,
        backgroundImage = backgroundImage,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        textAutoScale = textAutoScale,
        overlayParams = overlayParams
      )
    }

    fun roundedRectParams(
      context: Context,
      radius: Float,
      text: String = "",
      color: Int = Color.GRAY,
      borderThickness: Int = 0,
      width: Int = 0,
      height: Int = 0,
      font: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL),
      @ColorInt textColor: Int = Color.WHITE,
      fontSize: Float = 0f,
      margin: Float = context.dp2px(16).toFloat(),
      backgroundImage: Bitmap? = null,
      horizontalAlignment: HorizontalAlignment = HorizontalAlignment.CENTER,
      verticalAlignment: VerticalAlignment = VerticalAlignment.CENTER,
      textAutoScale: Boolean = true,
      overlayParams: OverlayParams = OverlayParams(Color.TRANSPARENT)
    ): NoteDrawableParams {
      val radii = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
      return NoteDrawableParams(
        context = context,
        text = text,
        color = color,
        borderThickness = borderThickness,
        width = width,
        height = height,
        font = font,
        shape = RoundRectShape(radii, null, null),
        textColor = textColor,
        fontSize = fontSize,
        margin = margin,
        radius = radius,
        backgroundImage = backgroundImage,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        textAutoScale = textAutoScale,
        overlayParams = overlayParams
      )
    }
  }
}
