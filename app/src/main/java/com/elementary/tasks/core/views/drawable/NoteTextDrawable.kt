package com.elementary.tasks.core.views.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import android.graphics.text.LineBreaker
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import timber.log.Timber

class NoteTextDrawable(
  private val params: NoteDrawableParams
) : ShapeDrawable(params.shape) {

  private val textPaint: TextPaint
  private val borderPaint: Paint
  private val overlayPaint: Paint
  private val color: Int
  private val shape: RectShape?
  private val height: Int
  private val width: Int
  private val borderThickness: Int

  init {

    // shape properties
    shape = params.shape
    height = params.height
    width = params.width

    // text and color
    color = params.color

    // text paint settings
    textPaint = TextPaint()
    textPaint.color = params.textColor
    textPaint.isAntiAlias = true
    textPaint.style = Paint.Style.FILL
    textPaint.typeface = params.font
    textPaint.textAlign = Paint.Align.CENTER
    textPaint.strokeWidth = params.borderThickness.toFloat()
    textPaint.textSize = params.fontSize.toFloat()

    // border paint settings
    borderThickness = params.borderThickness
    borderPaint = Paint()
    borderPaint.color = getDarkerShade(color)
    borderPaint.style = Paint.Style.STROKE
    borderPaint.strokeWidth = borderThickness.toFloat()

    // border paint settings
    overlayPaint = Paint()
    overlayPaint.color = params.overlayParams.color
    overlayPaint.style = Paint.Style.FILL

    // drawable paint color
    val paint = paint
    paint.color = color
  }

  private fun getDarkerShade(color: Int): Int {
    return Color.rgb(
      (SHADE_FACTOR * Color.red(color)).toInt(),
      (SHADE_FACTOR * Color.green(color)).toInt(),
      (SHADE_FACTOR * Color.blue(color)).toInt()
    )
  }

  private fun clipCenterPartOfBackgroundImage(src: Bitmap, radius: Float): Bitmap {
    val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = -0x1000000

    val path = getSeeThroughPath(src.width, src.height, radius)
    canvas.drawPath(path, paint)

    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
    canvas.drawBitmap(src, 0f, 0f, paint)

    return output
  }

  private fun getSeeThroughPath(width: Int, height: Int, radius: Float): Path {
    val path = Path()
    path.addRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, Path.Direction.CW)
    path.close()
    return path
  }

  override fun draw(canvas: Canvas) {
    super.draw(canvas)
    val r = bounds

    val image = params.backgroundImage
    if (image != null) {
      val scaledBitmap = Bitmap.createScaledBitmap(image, r.width(), r.height(), true)
      val src = Rect(0, 0, scaledBitmap.width, scaledBitmap.height)

      canvas.drawBitmap(clipCenterPartOfBackgroundImage(scaledBitmap, params.radius), src, r, null)

      // draw overlay
      drawOverlay(canvas)
    }

    // draw border
    if (borderThickness > 0) {
      drawBorder(canvas)
    }

    val count = canvas.save()
    canvas.translate(r.left.toFloat(), r.top.toFloat())

    // draw text
    val width = if (this.width < 0) r.width() else this.width
    val height = if (this.height < 0) r.height() else this.height

    val scaleStep = 0.5f

    val marginInt = params.margin.toInt()
    val expectedTextWidth = width - marginInt * 2

    var textLayout = createStaticLayout(params.text, expectedTextWidth)
    var textHeight = textLayout.height

    if (params.textAutoScale) {
      val textSize = textPaint.textSize
      val scaledTextSize = scaleText(
        targetHeight = height - marginInt,
        currentHeight = textHeight,
        text = params.text,
        textWidth = expectedTextWidth,
        scaleStep = scaleStep
      )

      if (textSize != scaledTextSize) {
        textPaint.textSize = scaledTextSize

        textLayout = createStaticLayout(params.text, expectedTextWidth)
        textHeight = textLayout.height
      }
    }

    val lineMaxWidth = textLayout.maxLineWith()

    val x: Float = when (params.horizontalAlignment) {
      NoteDrawableParams.HorizontalAlignment.CENTER -> width / 2f
      NoteDrawableParams.HorizontalAlignment.LEFT -> params.margin + (lineMaxWidth / 2)
      NoteDrawableParams.HorizontalAlignment.RIGHT -> width - params.margin - (lineMaxWidth / 2)
    }
    val y: Float = when (params.verticalAlignment) {
      NoteDrawableParams.VerticalAlignment.CENTER -> height / 2f - textHeight / 2f
      NoteDrawableParams.VerticalAlignment.BOTTOM -> height - textHeight.toFloat() - params.margin
      NoteDrawableParams.VerticalAlignment.TOP -> params.margin
    }

    canvas.save()
    canvas.translate(x, y)
    textLayout.draw(canvas)
    canvas.restore()

    canvas.restoreToCount(count)

    Timber.d("buildComplete")
  }

  private fun StaticLayout.maxLineWith(): Float {
    var maxWidth = 0f
    var i = 0
    while (i < lineCount) {
      val w = getLineWidth(i)
      if (w > maxWidth) {
        maxWidth = w
      }
      i++
    }
    return maxWidth
  }

  private fun createStaticLayout(
    text: String,
    textWidth: Int
  ) = StaticLayout.Builder.obtain(
    /* source = */ text,
    /* start = */ 0,
    /* end = */ text.length,
    /* paint = */ textPaint,
    /* width = */ textWidth
  )
    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
    .setTextDirection(TextDirectionHeuristics.LOCALE)
    .setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED)
    .build()

  private fun scaleText(
    targetHeight: Int,
    currentHeight: Int,
    text: String,
    textWidth: Int,
    scaleStep: Float
  ): Float {
    val heightOccupied = currentHeight / targetHeight.toFloat()
    return when {
      heightOccupied > 0.95f -> {
        scaleDown(targetHeight, text, textWidth, scaleStep)
      }

      heightOccupied < 0.65f -> {
        scaleUp(targetHeight, text, textWidth, scaleStep)
      }

      else -> textPaint.textSize
    }
  }

  private fun scaleDown(
    targetHeight: Int,
    text: String,
    textWidth: Int,
    scaleStep: Float
  ): Float {
    textPaint.textSize = textPaint.textSize - scaleStep
    val textLayout = createStaticLayout(text, textWidth)
    return scaleText(targetHeight, textLayout.height, text, textWidth, scaleStep)
  }

  private fun scaleUp(
    targetHeight: Int,
    text: String,
    textWidth: Int,
    scaleStep: Float
  ): Float {
    textPaint.textSize = textPaint.textSize + scaleStep
    val textLayout = createStaticLayout(text, textWidth)
    return scaleText(targetHeight, textLayout.height, text, textWidth, scaleStep)
  }

  private fun drawOverlay(canvas: Canvas) {
    val rect = RectF(bounds)
    when (shape) {
      is OvalShape -> canvas.drawOval(rect, overlayPaint)
      is RoundRectShape -> canvas.drawRoundRect(rect, params.radius, params.radius, overlayPaint)
      else -> canvas.drawRect(rect, overlayPaint)
    }
  }

  private fun drawBorder(canvas: Canvas) {
    val rect = RectF(bounds)
    rect.inset((borderThickness / 2).toFloat(), (borderThickness / 2).toFloat())

    when (shape) {
      is OvalShape -> canvas.drawOval(rect, borderPaint)
      is RoundRectShape -> canvas.drawRoundRect(rect, params.radius, params.radius, borderPaint)
      else -> canvas.drawRect(rect, borderPaint)
    }
  }

  override fun setAlpha(alpha: Int) {
    textPaint.alpha = alpha
  }

  override fun setColorFilter(cf: ColorFilter?) {
    textPaint.colorFilter = cf
  }

  @Deprecated("Deprecated in Java")
  override fun getOpacity(): Int {
    return PixelFormat.TRANSLUCENT
  }

  override fun getIntrinsicWidth(): Int {
    return width
  }

  override fun getIntrinsicHeight(): Int {
    return height
  }

  companion object {
    private val SHADE_FACTOR = 0.9f
  }
}
