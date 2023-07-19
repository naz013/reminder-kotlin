package com.elementary.tasks.core.views

import android.content.Context
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
import android.graphics.Typeface
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import android.graphics.text.LineBreaker
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.elementary.tasks.core.utils.ui.dp2px


class NoteTextDrawable private constructor(
  private val builder: Builder
) : ShapeDrawable(builder.shape) {

  private val textPaint: TextPaint
  private val borderPaint: Paint
  private val text: String?
  private val color: Int
  private val shape: RectShape?
  private val height: Int
  private val width: Int
  private val fontSize: Int
  private val borderThickness: Int

  init {

    // shape properties
    shape = builder.shape
    height = builder.height
    width = builder.width

    // text and color
    text = if (builder.toUpperCase) builder.text!!.uppercase() else builder.text
    color = builder.color

    // text paint settings
    fontSize = builder.fontSize
    textPaint = TextPaint()
    textPaint.color = builder.textColor
    textPaint.isAntiAlias = true
    textPaint.style = Paint.Style.FILL
    textPaint.typeface = builder.font
    textPaint.textAlign = Paint.Align.CENTER
    textPaint.strokeWidth = builder.borderThickness.toFloat()
    textPaint.textSize = builder.fontSize.toFloat()

    // border paint settings
    borderThickness = builder.borderThickness
    borderPaint = Paint()
    borderPaint.color = getDarkerShade(color)
    borderPaint.style = Paint.Style.STROKE
    borderPaint.strokeWidth = borderThickness.toFloat()

    // drawable paint color
    val paint = paint
    paint.color = color
  }

  private fun getDarkerShade(color: Int): Int {
    return Color.rgb((SHADE_FACTOR * Color.red(color)).toInt(),
      (SHADE_FACTOR * Color.green(color)).toInt(),
      (SHADE_FACTOR * Color.blue(color)).toInt())
  }

  private fun clipCenterPartOfBackgroundImage(src: Bitmap, radius: Float) : Bitmap {
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

    val image = builder.imageBg
    if (image != null) {
      val src = Rect(0, 0, image.width, image.height)
      canvas.drawBitmap(clipCenterPartOfBackgroundImage(image, builder.radius), src, r, null)
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

    val margin = builder.context.dp2px(16)
    val scaleStep = 0.5f

    val textWidth = width - margin * 2

    var textLayout = createStaticLayout(text!!, textWidth)
    var textHeight = textLayout.height

    val textSize = textPaint.textSize
    val scaledTextSize = scaleText(
      targetHeight = height - margin,
      currentHeight = textHeight,
      text = text,
      textWidth = textWidth,
      scaleStep = scaleStep
    )

    if (textSize != scaledTextSize) {
      textPaint.textSize = scaledTextSize

      textLayout = createStaticLayout(text, textWidth)
      textHeight = textLayout.height
    }

    val x: Float = width / 2f
    val y: Float = height / 2f - textHeight / 2f

    canvas.save()
    canvas.translate(x, y)
    textLayout.draw(canvas)
    canvas.restore()

    canvas.restoreToCount(count)
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

  private fun drawBorder(canvas: Canvas) {
    val rect = RectF(bounds)
    rect.inset((borderThickness / 2).toFloat(), (borderThickness / 2).toFloat())

    when (shape) {
      is OvalShape -> canvas.drawOval(rect, borderPaint)
      is RoundRectShape -> canvas.drawRoundRect(rect, builder.radius, builder.radius, borderPaint)
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

  class Builder(val context: Context) : IConfigBuilder, IShapeBuilder, IBuilder {

    var text: String? = null

    var color: Int = 0

    var borderThickness: Int = 0

    var width: Int = 0

    var height: Int = 0

    var font: Typeface? = null

    var shape: RectShape? = null

    var textColor: Int = 0

    var fontSize: Int = 0

    var isBold: Boolean = false

    var toUpperCase: Boolean = false

    var radius: Float = 0.toFloat()

    var imageBg: Bitmap? = null

    init {
      text = ""
      color = Color.GRAY
      textColor = Color.WHITE
      borderThickness = 0
      width = -1
      height = -1
      shape = RectShape()
      font = Typeface.create("sans-serif-light", Typeface.NORMAL)
      fontSize = -1
      isBold = false
      toUpperCase = false
      imageBg = null
    }

    override fun width(width: Int): IConfigBuilder {
      this.width = width
      return this
    }

    override fun height(height: Int): IConfigBuilder {
      this.height = height
      return this
    }

    override fun textColor(color: Int): IConfigBuilder {
      this.textColor = color
      return this
    }

    override fun withBorder(thickness: Int): IConfigBuilder {
      this.borderThickness = thickness
      return this
    }

    override fun useFont(font: Typeface): IConfigBuilder {
      this.font = font
      return this
    }

    override fun fontSize(size: Int): IConfigBuilder {
      this.fontSize = size
      return this
    }

    override fun withImage(image: Bitmap): IConfigBuilder {
      this.imageBg = image
      return this
    }

    override fun bold(): IConfigBuilder {
      this.isBold = true
      return this
    }

    override fun toUpperCase(): IConfigBuilder {
      this.toUpperCase = true
      return this
    }

    override fun beginConfig(): IConfigBuilder {
      return this
    }

    override fun endConfig(): IShapeBuilder {
      return this
    }

    private fun rect(): IBuilder {
      this.shape = RectShape()
      return this
    }

    private fun round(): IBuilder {
      this.shape = OvalShape()
      return this
    }

    private fun roundRect(radius: Int): IBuilder {
      this.radius = radius.toFloat()
      val radii = floatArrayOf(radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat(), radius.toFloat())
      this.shape = RoundRectShape(radii, null, null)
      return this
    }

    override fun buildRect(text: String, color: Int): NoteTextDrawable {
      rect()
      return build(text, color)
    }

    override fun buildRoundRect(text: String, color: Int, radius: Int): NoteTextDrawable {
      roundRect(radius)
      return build(text, color)
    }

    override fun buildRound(text: String, color: Int): NoteTextDrawable {
      round()
      return build(text, color)
    }

    override fun build(text: String, color: Int): NoteTextDrawable {
      this.color = color
      this.text = text
      return NoteTextDrawable(this)
    }
  }

  interface IConfigBuilder {
    fun width(width: Int): IConfigBuilder

    fun height(height: Int): IConfigBuilder

    fun textColor(color: Int): IConfigBuilder

    fun withBorder(thickness: Int): IConfigBuilder

    fun useFont(font: Typeface): IConfigBuilder

    fun fontSize(size: Int): IConfigBuilder

    fun withImage(image: Bitmap): IConfigBuilder

    fun bold(): IConfigBuilder

    fun toUpperCase(): IConfigBuilder

    fun endConfig(): IShapeBuilder
  }

  interface IBuilder {

    fun build(text: String, color: Int): NoteTextDrawable
  }

  interface IShapeBuilder {

    fun beginConfig(): IConfigBuilder

    fun buildRect(text: String, color: Int): NoteTextDrawable

    fun buildRoundRect(text: String, color: Int, radius: Int): NoteTextDrawable

    fun buildRound(text: String, color: Int): NoteTextDrawable
  }

  companion object {
    private val SHADE_FACTOR = 0.9f

    fun builder(context: Context): IShapeBuilder {
      return Builder(context)
    }
  }
}
