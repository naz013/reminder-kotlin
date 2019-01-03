package com.elementary.tasks.core.drawing

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.elementary.tasks.core.interfaces.Observable
import com.elementary.tasks.core.interfaces.Observer
import com.elementary.tasks.core.utils.AssetsUtil
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.*

/*
  CanvasView.java
  <p>
  Copyright (c) 2014 Tomohiro IKEDA (Korilakkuma)
  Released under the MIT license
 */

/**
 * This class defines fields and methods for drawing.
 */
class DrawView : View, Observable {

    private val observers = ArrayList<Observer>()

    val elements = ArrayList<Drawing>()

    /**
     * This method is getter for canvas background color
     *
     * @return
     */
    /**
     * This method is setter for canvas background color
     *
     * @param color
     */
    @ColorInt
    @get:ColorInt
    var baseColor = Color.WHITE
        set(@ColorInt color) {
            field = color
            val element = elements[0]
            if (element is Background) {
                element.color = this.baseColor
            }
            this.invalidate()
        }

    // for Undo, Redo
    private var historyPointer = 0

    // Flags
    /**
     * This method is getter for mode.
     *
     * @return
     */
    /**
     * This method is setter for mode.
     *
     * @param mode
     */
    var mode = Mode.DRAW
    /**
     * This method is getter for drawer.
     *
     * @return
     */
    /**
     * This method is setter for drawer.
     *
     * @param drawer
     */
    var drawer = Drawer.PEN
    private var isDown = false

    // for Paint
    /**
     * This method is getter for stroke or fill.
     *
     * @return
     */
    /**
     * This method is setter for stroke or fill.
     *
     * @param style
     */
    var paintStyle: Paint.Style = Paint.Style.STROKE
    /**
     * This method is getter for stroke color.
     *
     * @return
     */
    /**
     * This method is setter for stroke color.
     *
     * @param color
     */
    @ColorInt
    @get:ColorInt
    var paintStrokeColor = Color.BLACK
        set(@ColorInt color) {
            field = color
            if (this.mode == Mode.TEXT) {
                val drawing = current
                if (drawing is Text) {
                    drawing.setTextColor(color)
                    this.invalidate()
                }
            }
        }
    /**
     * This method is getter for fill color.
     * But, current Android API cannot set fill color (?).
     *
     * @return
     */
    /**
     * This method is setter for fill color.
     * But, current Android API cannot set fill color (?).
     *
     * @param color
     */
    @ColorInt
    @get:ColorInt
    var paintFillColor = Color.BLACK
    private var paintStrokeWidth = 3f
    private var opacity = 255
    /**
     * This method is getter for amount of blur.
     *
     * @return
     */
    /**
     * This method is setter for amount of blur.
     * The 1st argument is greater than or equal to 0.0.
     *
     * @param blur
     */
    var blur = 0f
        set(blur) = if (blur >= 0) {
            field = blur
        } else {
            field = 0f
        }
    /**
     * This method is getter for line cap.
     *
     * @return
     */
    /**
     * This method is setter for line cap.
     *
     * @param cap
     */
    var lineCap: Paint.Cap = Paint.Cap.ROUND
    /**
     * This method is getter for path effect of drawing.
     *
     * @return drawPathEffect
     */
    /**
     * This method is setter for path effect of drawing.
     *
     * @param drawPathEffect
     */
    var drawPathEffect: PathEffect? = null

    private var fontFamily = 9
    private var fontSize = 32f
    private val textAlign = Paint.Align.RIGHT  // fixed
    private val textPaint = Paint()
    private var mCanvas: Canvas? = null

    // for Drawer
    private var startX = 0f
    private var startY = 0f
    private var controlX = 0f
    private var controlY = 0f
    private var bmpStartX = 0f
    private var bmpStartY = 0f

    private var currentItem: Drawing? = null
    private var mCallback: DrawCallback? = null

    /**
     * This method gets the instance of Path that pointer indicates.
     *
     * @return the instance of Path
     */
    private val currentPath: Path?
        get() {
            val drawing = this.elements[this.historyPointer - 1]
            return if (drawing is Figure) {
                drawing.path
            } else null
        }

    private val current: Drawing
        get() = this.elements[this.historyPointer - 1]

    val scale: Int
        get() {
            val drawing = current
            return (drawing as? Image)?.percentage ?: 100
        }

    /**
     * This method gets current canvas as bitmap.
     *
     * @return This is returned as bitmap.
     */
    var bitmap: Bitmap
        get() {
            this.isDrawingCacheEnabled = false
            this.isDrawingCacheEnabled = true
            return Bitmap.createBitmap(this.drawingCache)
        }
        set(bitmap) {
            this.currentItem = current
            if (currentItem is Image) {
                (currentItem as Image).setBitmap(bitmap)
            }
            this.invalidate()
        }

    /**
     * This method gets the bitmap as byte array.
     * Bitmap format is PNG, and quality is 100.
     *
     * @return This is returned as byte array of bitmap.
     */
    val bitmapAsByteArray: ByteArray
        get() = this.getBitmapAsByteArray(CompressFormat.PNG, 100)

    override fun addObserver(observer: Observer) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
    }

    override fun removeObserver(observer: Observer) {
        if (observers.contains(observer)) {
            observers.remove(observer)
        }
    }

    override fun notifyObservers() {
        for (observer in observers) {
            observer.setUpdate(this.historyPointer)
        }
    }

    // Enumeration for Mode
    enum class Mode {
        DRAW,
        TEXT,
        IMAGE,
        LAYERS,
        ERASER
    }

    // Enumeration for Drawer
    enum class Drawer {
        PEN,
        LINE,
        RECTANGLE,
        CIRCLE,
        ELLIPSE,
        FILL,
        QUADRATIC_BEZIER
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        this.setup()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.setup()
    }

    constructor(context: Context) : super(context) {
        this.setup()
    }

    fun setHistoryPointer(historyPointer: Int) {
        this.historyPointer = historyPointer
        this.invalidate()
    }

    fun getHistoryPointer(): Int {
        return historyPointer
    }

    fun getElements(): List<Drawing> {
        return elements
    }

    /**
     * Common initialization.
     */
    private fun setup() {
        this.historyPointer = 0
        this.elements.clear()
        this.elements.add(Background(this.baseColor))
        this.historyPointer++
        this.textPaint.setARGB(0, 255, 255, 255)
    }

    /**
     * This method creates the instance of Paint.
     * In addition, this method sets styles for Paint.
     *
     * @return paint This is returned as the instance of Paint
     */
    private fun createPaint(): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = this.paintStyle
        paint.strokeWidth = this.paintStrokeWidth
        paint.strokeCap = this.lineCap
        paint.strokeJoin = Paint.Join.MITER  // fixed
        // for Text
        if (this.mode == Mode.TEXT) {
            paint.typeface = AssetsUtil.getTypeface(context, this.fontFamily)
            paint.textSize = this.fontSize
            paint.textAlign = this.textAlign
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 0f
        }
        if (this.mode == Mode.ERASER) {
            // Eraser
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            paint.setARGB(0, 0, 0, 0)
            paint.color = this.baseColor
            // paint.setShadowLayer(this.blur, 0F, 0F, this.baseColor);
        } else {
            // Otherwise
            paint.color = this.paintStrokeColor
            paint.setShadowLayer(this.blur, 0f, 0f, this.paintStrokeColor)
            paint.alpha = this.opacity
            paint.pathEffect = this.drawPathEffect
        }
        return paint
    }

    /**
     * This method initialize Path.
     * Namely, this method creates the instance of Path,
     * and moves current position.
     *
     * @param event This is argument of onTouchEvent method
     * @return path This is returned as the instance of Path
     */
    private fun createPath(event: MotionEvent): Path {
        val path = Path()
        // Save for ACTION_MOVE
        this.startX = event.x
        this.startY = event.y
        path.moveTo(this.startX, this.startY)
        return path
    }

    /**
     * This method updates the lists for the instance of Path and Paint.
     * "Undo" and "Redo" are enabled by this method.
     *
     * @param path the instance of Path
     */
    private fun updateHistory(path: Path) {
        if (this.historyPointer == this.elements.size) {
            this.elements.add(Figure(path, createPaint()))
            this.historyPointer++
        } else {
            this.elements[this.historyPointer] = Figure(path, createPaint())
            this.historyPointer++
            var i = this.historyPointer
            val size = this.elements.size
            while (i < size) {
                this.elements.removeAt(this.historyPointer)
                i++
            }
        }
    }

    private fun sendCallback() {
        if (mCallback != null) {
            mCallback!!.onDrawEnd()
        }
    }

    /**
     * This method defines processes on MotionEvent.ACTION_DOWN
     *
     * @param event This is argument of onTouchEvent method
     */
    private fun onActionDown(event: MotionEvent) {
        when (this.mode) {
            Mode.DRAW, Mode.ERASER -> if (this.drawer != Drawer.QUADRATIC_BEZIER) {
                this.updateHistory(this.createPath(event))
                this.isDown = true
            } else {
                if (this.startX == 0f && this.startY == 0f) {
                    this.updateHistory(this.createPath(event))
                } else {
                    this.controlX = event.x
                    this.controlY = event.y
                    this.isDown = true
                }
            }
            Mode.TEXT, Mode.IMAGE -> {
                this.startX = event.x
                this.startY = event.y
                this.bmpStartX = currentItem!!.x
                this.bmpStartY = currentItem!!.y
            }
            else -> {
            }
        }
    }

    /**
     * This method defines processes on MotionEvent.ACTION_MOVE
     *
     * @param event This is argument of onTouchEvent method
     */
    private fun onActionMove(event: MotionEvent) {
        val x = event.x
        val y = event.y
        when (this.mode) {
            Mode.IMAGE -> moveBitmap(x, y)
            Mode.DRAW, Mode.ERASER -> if (this.drawer != Drawer.QUADRATIC_BEZIER) {
                if (!isDown) {
                    return
                }
                val path = this.currentPath ?: return
                when (this.drawer) {
                    Drawer.PEN -> path.lineTo(x, y)
                    Drawer.FILL -> {
                        baseColor = this.paintFillColor
                    }
                    Drawer.LINE -> {
                        path.reset()
                        path.moveTo(this.startX, this.startY)
                        path.lineTo(x, y)
                    }
                    Drawer.RECTANGLE -> {
                        path.reset()
                        val left = Math.min(this.startX, x)
                        val right = Math.max(this.startX, x)
                        val top = Math.min(this.startY, y)
                        val bottom = Math.max(this.startY, y)
                        path.addRect(left, top, right, bottom, Path.Direction.CCW)
                    }
                    Drawer.CIRCLE -> {
                        val distanceX = Math.abs((this.startX - x).toDouble())
                        val distanceY = Math.abs((this.startY - y).toDouble())
                        val radius = Math.sqrt(Math.pow(distanceX, 2.0) + Math.pow(distanceY, 2.0))
                        path.reset()
                        path.addCircle(this.startX, this.startY, radius.toFloat(), Path.Direction.CCW)
                    }
                    Drawer.ELLIPSE -> {
                        val rect = RectF(this.startX, this.startY, x, y)
                        path.reset()
                        path.addOval(rect, Path.Direction.CCW)
                    }
                    else -> {
                    }
                }
            } else {
                if (!isDown) {
                    return
                }
                val path = this.currentPath ?: return
                path.reset()
                path.moveTo(this.startX, this.startY)
                path.quadTo(this.controlX, this.controlY, x, y)
            }
            Mode.TEXT -> moveText(x, y)
            else -> {
            }
        }
    }

    private fun moveText(x: Float, y: Float) {
        val drawing = current
        if (drawing is Text) {
            drawing.x = x
            drawing.y = y
            this.invalidate()
        }
    }

    private fun moveBitmap(x: Float, y: Float) {
        val drawing = current
        if (drawing is Image) {
            drawing.x = bmpStartX - (startX - x)
            drawing.y = bmpStartY - (startY - y)
            this.invalidate()
        }
    }

    /**
     * This method defines processes on MotionEvent.ACTION_DOWN
     */
    private fun onActionUp() {
        if (isDown) {
            this.startX = 0f
            this.startY = 0f
            this.isDown = false
        }
        sendCallback()
    }

    /**
     * This method updates the instance of Canvas (View)
     *
     * @param canvas the new instance of Canvas
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (this.elements != null && !this.elements.isEmpty()) {
            for (i in 0 until historyPointer) {
                try {
                    elements[i].draw(canvas, false)
                } catch (ignored: IndexOutOfBoundsException) {
                }

            }
        }
        this.mCanvas = canvas
        notifyObservers()
    }

    /**
     * This method set event listener for drawing.
     *
     * @param event the instance of MotionEvent
     * @return
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> this.onActionDown(event)
            MotionEvent.ACTION_MOVE -> this.onActionMove(event)
            MotionEvent.ACTION_UP -> this.onActionUp()
            else -> {
            }
        }
        // Re draw
        this.invalidate()
        return true
    }

    fun setCallback(mCallback: DrawCallback) {
        this.mCallback = mCallback
    }

    /**
     * This method checks if Undo is available
     *
     * @return If Undo is available, this is returned as true. Otherwise, this is returned as false.
     */
    fun canUndo(): Boolean {
        return this.historyPointer > 1
    }

    /**
     * This method checks if Redo is available
     *
     * @return If Redo is available, this is returned as true. Otherwise, this is returned as false.
     */
    fun canRedo(): Boolean {
        return this.historyPointer < this.elements.size
    }

    /**
     * This method draws canvas again for Undo.
     *
     * @return If Undo is enabled, this is returned as true. Otherwise, this is returned as false.
     */
    fun undo(): Boolean {
        if (canUndo()) {
            this.historyPointer--
            this.currentItem = current
            this.invalidate()
            sendCallback()
            return true
        } else {
            return false
        }
    }

    /**
     * This method draws canvas again for Redo.
     *
     * @return If Redo is enabled, this is returned as true. Otherwise, this is returned as false.
     */
    fun redo(): Boolean {
        if (canRedo()) {
            this.historyPointer++
            this.currentItem = current
            this.invalidate()
            sendCallback()
            return true
        } else {
            return false
        }
    }

    /**
     * This method initializes canvas.
     *
     * @return
     */
    fun clear() {
        Timber.d("clear: ")
        this.setup()
        this.invalidate()
        sendCallback()
    }

    /**
     * This method is setter for drawn text.
     *
     * @param text
     */
    fun addText(text: String) {
        this.currentItem = Text(text, fontSize, createPaint())
        this.currentItem?.x = startX
        this.currentItem?.y = startY
        if (this.historyPointer == this.elements.size) {
            this.elements.add(currentItem!!)
        } else {
            this.elements.add(historyPointer, currentItem!!)
        }
        this.historyPointer++
        this.invalidate()
    }

    fun setText(text: String) {
        this.currentItem = current
        if (currentItem is Text) {
            (currentItem as Text).text = text
            this.invalidate()
        }
    }

    /**
     * This method is getter for stroke width.
     *
     * @return
     */
    fun getPaintStrokeWidth(): Float {
        val drawing = current
        return (drawing as? Figure)?.strokeWidth ?: this.paintStrokeWidth
    }

    /**
     * This method is setter for stroke width.
     *
     * @param width
     */
    fun setPaintStrokeWidth(width: Float) {
        if (width >= 0) {
            this.paintStrokeWidth = width
        } else {
            this.paintStrokeWidth = 3f
        }
        val drawing = current
        if (drawing is Figure) {
            drawing.strokeWidth = this.paintStrokeWidth
            this.invalidate()
        }
    }

    /**
     * This method is getter for alpha.
     *
     * @return
     */
    fun getOpacity(): Int {
        val drawing = current
        return drawing.opacity
        //        return this.opacity;
    }

    /**
     * This method is setter for alpha.
     * The 1st argument must be between 0 and 255.
     *
     * @param opacity
     */
    fun setOpacity(opacity: Int, mode: Mode) {
        if (opacity in 0..255) {
            this.opacity = opacity
        } else {
            this.opacity = 255
        }
        val drawing = current
        if (mode == Mode.DRAW && drawing is Figure) {
            drawing.opacity = this.opacity
        } else if (mode == Mode.TEXT && drawing is Text) {
            drawing.opacity = this.opacity
        } else if (mode == Mode.IMAGE && (drawing is Background || drawing is Image)) {
            drawing.opacity = this.opacity
        }
        this.invalidate()
    }

    fun setScale(scale: Int, mode: Mode) {
        if (scale in 1..100) {
            val drawing = current
            if (mode == Mode.IMAGE && drawing is Image) {
                drawing.setScalePercentage(scale)
            }
        }
        this.invalidate()
    }

    /**
     * This method is getter for font size,
     *
     * @return
     */
    fun getFontSize(): Float {
        val drawing = current
        return (drawing as? Text)?.getFontSize() ?: this.fontSize
    }

    /**
     * This method is setter for font size.
     * The 1st argument is greater than or equal to 0.0.
     *
     * @param size
     */
    fun setFontSize(size: Float) {
        if (size >= 0f) {
            this.fontSize = size
        } else {
            this.fontSize = 32f
        }
        val drawing = current
        if (drawing is Text) {
            drawing.setFontSize(this.fontSize)
            this.invalidate()
        }
    }

    /**
     * This method is getter for font-family.
     *
     * @return
     */
    fun getFontFamily(): Int {
        val drawing = current
        return (drawing as? Text)?.fontFamily ?: this.fontFamily
    }

    /**
     * This method is setter for font-family.
     */
    fun setFontFamily(position: Int) {
        this.fontFamily = position
        val drawing = current
        if (drawing is Text) {
            drawing.setFontFamily(this.fontFamily, AssetsUtil.getTypeface(context, position))
            this.invalidate()
        }
    }

    /**
     * This method gets current canvas as scaled bitmap.
     *
     * @return This is returned as scaled bitmap.
     */
    fun getScaleBitmap(w: Int, h: Int): Bitmap {
        this.isDrawingCacheEnabled = false
        this.isDrawingCacheEnabled = true
        return Bitmap.createScaledBitmap(this.drawingCache, w, h, true)
    }

    /**
     * This method draws the designated bitmap to canvas.
     *
     * @param bitmap
     */
    fun addBitmap(bitmap: Bitmap) {
        this.currentItem = Image(bitmap)
        if (this.historyPointer == this.elements.size) {
            this.elements.add(currentItem!!)
        } else {
            this.elements.add(historyPointer, currentItem!!)
        }
        this.historyPointer++
        if (mCanvas != null) {
        }
        this.invalidate()
    }

    /**
     * This method draws the designated byte array of bitmap to canvas.
     *
     * @param byteArray This is returned as byte array of bitmap.
     */
    fun addBitmap(byteArray: ByteArray) {
        this.addBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size))
    }

    fun setBitmap(byteArray: ByteArray) {
        this.bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    /**
     * This method gets the bitmap as byte array.
     *
     * @param format
     * @param quality
     * @return This is returned as byte array of bitmap.
     */
    fun getBitmapAsByteArray(format: CompressFormat, quality: Int): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        this.bitmap.compress(format, quality, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    interface DrawCallback {
        fun onDrawEnd()
    }

    companion object {

        /**
         * This static method gets the designated bitmap as byte array.
         *
         * @param bitmap
         * @param format
         * @param quality
         * @return This is returned as byte array of bitmap.
         */
        fun getBitmapAsByteArray(bitmap: Bitmap, format: CompressFormat, quality: Int): ByteArray {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(format, quality, byteArrayOutputStream)
            return byteArrayOutputStream.toByteArray()
        }
    }
}
