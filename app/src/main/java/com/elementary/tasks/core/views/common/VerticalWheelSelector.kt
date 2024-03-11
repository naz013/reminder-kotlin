package com.elementary.tasks.core.views.common

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import com.elementary.tasks.R

class VerticalWheelSelector : View {

  var onSelectionChangedListener: OnSelectionChangedListener? = null
  var selectedItemPosition = 0
    private set

  private val linePaint = Paint().apply {
    isAntiAlias = true
    color = Color.RED
    strokeWidth = 2f
    style = Paint.Style.STROKE
    textAlign = Paint.Align.CENTER
  }
  private val selectedPaint = TextPaint().apply {
    isAntiAlias = true
    color = Color.BLACK
    textSize = 60f
    textAlign = Paint.Align.CENTER
  }
  private val defaultPaint = TextPaint().apply {
    set(selectedPaint)
    color = Color.GRAY
    textSize = selectedPaint.textSize * 0.8f
    textAlign = Paint.Align.CENTER
  }

  private var viewParams = ViewParams(0, 0)
  private var numberOfVisibleRows = DEFAULT_NUM_ROWS
  private var items: List<Item> = emptyList()
  private val rectangles = mutableListOf<ItemData>()
  private var maxScrollY = 0f
  private var scrollY = 0f

  private var centerX = 0f
  private var centerY = 0f

  private val gestureListener = GestureListener()
  private var gestureDetector: GestureDetector

  constructor(context: Context) : this(context, null)

  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

  @SuppressLint("ClickableViewAccessibility")
  constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    gestureDetector = GestureDetector(context, gestureListener)
    setOnTouchListener { v, event ->
      if (gestureDetector.onTouchEvent(event)) {
        return@setOnTouchListener true
      }
      return@setOnTouchListener processTouch(event)
    }

    if (attrs != null) {
      val textView: TextView = AppCompatTextView(context)
      val a = context.theme.obtainStyledAttributes(
        attrs,
        R.styleable.VerticalWheelSelector,
        defStyle,
        0
      )
      try {
        if (a.hasValue(R.styleable.VerticalWheelSelector_wheel_visibleRowsCount)) {
          numberOfVisibleRows = a.getInt(
            /* index = */ R.styleable.VerticalWheelSelector_wheel_visibleRowsCount,
            /* defValue = */ DEFAULT_NUM_ROWS
          )
        }
        if (a.hasValue(R.styleable.VerticalWheelSelector_wheel_items)) {
          val id = a.getResourceId(R.styleable.VerticalWheelSelector_wheel_items, 0)
          if (id != 0) {
            resources.getStringArray(id).map { Item(it) }
              .also { this.items = it }
          }
        }
        if (a.hasValue(R.styleable.VerticalWheelSelector_wheel_defaultTextAppearance)) {
          val style = a.getResourceId(
            /* index = */ R.styleable.VerticalWheelSelector_wheel_defaultTextAppearance,
            /* defValue = */ 0
          )
          if (style != 0) {
            TextViewCompat.setTextAppearance(textView, style)
            defaultPaint.set(textView.paint)
          }
        }
        if (a.hasValue(R.styleable.VerticalWheelSelector_wheel_selectedTextAppearance)) {
          val style = a.getResourceId(
            /* index = */ R.styleable.VerticalWheelSelector_wheel_selectedTextAppearance,
            /* defValue = */ 0
          )
          if (style != 0) {
            TextViewCompat.setTextAppearance(textView, style)
            selectedPaint.set(textView.paint)
          }
        }
        if (a.hasValue(R.styleable.VerticalWheelSelector_wheel_defaultTextColor)) {
          val color = a.getColor(
            /* index = */ R.styleable.VerticalWheelSelector_wheel_defaultTextColor,
            /* defValue = */ 0
          )
          defaultPaint.color = color
        }
        if (a.hasValue(R.styleable.VerticalWheelSelector_wheel_selectedTextColor)) {
          val color = a.getColor(
            /* index = */ R.styleable.VerticalWheelSelector_wheel_selectedTextColor,
            /* defValue = */ 0
          )
          selectedPaint.color = color
        }
        if (a.hasValue(R.styleable.VerticalWheelSelector_wheel_defaultTextSize)) {
          val textSize = a.getDimensionPixelSize(
            /* index = */ R.styleable.VerticalWheelSelector_wheel_defaultTextSize,
            /* defValue = */ 0
          )
          defaultPaint.textSize = textSize.toFloat()
        }
        if (a.hasValue(R.styleable.VerticalWheelSelector_wheel_selectedTextSize)) {
          val textSize = a.getDimensionPixelSize(
            /* index = */ R.styleable.VerticalWheelSelector_wheel_selectedTextSize,
            /* defValue = */ 0
          )
          selectedPaint.textSize = textSize.toFloat()
        }
        if (a.hasValue(R.styleable.VerticalWheelSelector_wheel_defaultTextStyle)) {
          val textStyle = a.getInt(
            /* index = */ R.styleable.VerticalWheelSelector_wheel_defaultTextStyle,
            /* defValue = */ 0
          )
          updateTextStyle(textStyle, defaultPaint)
        }
        if (a.hasValue(R.styleable.VerticalWheelSelector_wheel_selectedTextStyle)) {
          val textStyle = a.getInt(
            /* index = */ R.styleable.VerticalWheelSelector_wheel_selectedTextStyle,
            /* defValue = */ 0
          )
          updateTextStyle(textStyle, selectedPaint)
        }
      } catch (e: Exception) {
        log("initError: ${e.localizedMessage}")
      } finally {
        a.recycle()
      }
    }
  }

  private fun updateTextStyle(textStyle: Int, paint: TextPaint) {
    val typefaceStyle = paint.typeface.style
    val need: Int = textStyle and typefaceStyle.inv()
    paint.isFakeBoldText = need and Typeface.BOLD != 0
    paint.textSkewX = if (need and Typeface.ITALIC != 0) {
      -0.25f
    } else {
      0f
    }
  }

  private fun processTouch(event: MotionEvent): Boolean {
    if (event.action == MotionEvent.ACTION_DOWN) {
      return true
    } else if (event.action == MotionEvent.ACTION_UP) {
      animateDragY()
      return true
    }
    return false
  }

  fun setItems(items: List<String>) {
    this.items = items.map { Item(it) }
    this.calculateRectangles()
    this.invalidate()
    log("setItems: items=${this.items.size}, rectangles=${this.rectangles.size}")
  }

  fun setNumberOfVisibleRows(@IntRange(from = 3L, to = 9L) numberOfRows: Int) {
    if (numberOfRows < 3L || numberOfRows > 9L) {
      throw IllegalArgumentException("Number of rows must be between 3 and 9")
    }
    this.numberOfVisibleRows = numberOfRows
    this.invalidate()
  }

  fun selectItem(position: Int) {
    log("selectItem: position=$position, rectangles=${rectangles.size}, items=${items.size}")
    if (position < 0 || position >= items.size) {
      throw IllegalArgumentException("Position must be between 0 and ${items.size - 1}")
    }

    selectedItemPosition = position

    if (position >= rectangles.size) {
      return
    }

    val item = rectangles[position]
    if (item.selected) {
      return
    }
    val distanceY = item.rectF.centerY() - centerY
    animateDragY(distanceY)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
//    canvas.drawColor(Color.LTGRAY)
    rectangles.forEachIndexed { index, data ->
      if (data.selected) {
//        canvas.drawRect(data.rectF, linePaint)
        canvas.drawText(
          /* text = */ data.item.text,
          /* x = */ data.selectedTextPointF.x,
          /* y = */ data.selectedTextPointF.y,
          /* paint = */ selectedPaint
        )
      } else {
//        canvas.drawRect(data.rectF, linePaint)
        canvas.drawText(
          /* text = */ data.item.text,
          /* x = */ data.unselectedTextPointF.x,
          /* y = */ data.unselectedTextPointF.y,
          /* paint = */ defaultPaint
        )
      }
    }
//    canvas.drawLine(0f, centerY, viewParams.width.toFloat(), centerY, linePaint)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val width = MeasureSpec.getSize(widthMeasureSpec)
    val height = MeasureSpec.getSize(heightMeasureSpec)
    viewParams.width = width
    viewParams.height = height

    val rowHeight = height.toFloat() / numberOfVisibleRows
    val rowWidth = width.toFloat()
    viewParams.rowHeight = rowHeight
    viewParams.rowWidth = rowWidth

    if (rectangles.isEmpty() && items.isNotEmpty()) {
      calculateRectangles()
      if (selectedItemPosition > 0) {
        selectItem(selectedItemPosition)
      }
    }

    setMeasuredDimension(width, height)
  }

  private fun calculateRectangles() {
    log("calculateRectangles: viewParams=$viewParams, items=${items.size}")
    if (viewParams.height == 0 || viewParams.width == 0 || items.isEmpty()) {
      return
    }

    val rowHeight = viewParams.rowHeight

    val numberOfRectangles = items.size
    val numberOfEmptyRows = numberOfVisibleRows / 2f
    var top = (numberOfEmptyRows - 0.5f) * rowHeight
    var bottom = top + rowHeight

    centerX = viewParams.width / 2f
    centerY = viewParams.height / 2f

    maxScrollY =
      (numberOfRectangles * rowHeight) - rowHeight + (numberOfRectangles * DIVIDER_HEIGHT)
    rectangles.clear()

    val textBounds = Rect()

    for (i in 0 until numberOfRectangles) {
      val rect = RectF(0f, top, viewParams.rowWidth, bottom)

      val text = items[i].text

      selectedPaint.getTextBounds(text, 0, text.length, textBounds)
      val selectedTextPointF = PointF(
        rect.centerX() - (textBounds.width() / 2f),
        rect.centerY() + (textBounds.height() / 3f)
      )

      defaultPaint.getTextBounds(text, 0, text.length, textBounds)
      val unselectedTextPointF = PointF(
        rect.centerX() - (textBounds.width() / 2f),
        rect.centerY() + (textBounds.height() / 3f)
      )

      rectangles.add(
        ItemData(
          rectF = rect,
          item = items[i],
          selectedTextPointF = selectedTextPointF,
          unselectedTextPointF = unselectedTextPointF,
          selected = isSelected(rect, centerX, centerY)
        )
      )
      top += viewParams.rowHeight + DIVIDER_HEIGHT
      bottom = top + viewParams.rowHeight
    }
  }

  private fun computeSingleTap(x: Float, y: Float) {
    val index = findItemPosition(x, y)
    if (index == -1 || index >= rectangles.size) {
      return
    }
    selectItem(index)
  }

  private fun findItemPosition(x: Float, y: Float): Int {
    return rectangles.indexOfFirst { isSelected(it.rectF, x, y) }
  }

  private fun computeScrollY(distanceY: Float) {
    if (viewParams.height == 0 || viewParams.width == 0 || rectangles.isEmpty()) {
      return
    }
    val oldScrollY = scrollY
    val newScrollY = scrollY + distanceY
    scrollY = if (newScrollY > maxScrollY) {
      maxScrollY
    } else if (newScrollY < 0) {
      0f
    } else {
      newScrollY
    }
    val fixedDistanceY = scrollY - oldScrollY

    rectangles.forEachIndexed { index, data ->
      data.rectF.top -= fixedDistanceY
      data.rectF.bottom -= fixedDistanceY
      data.selectedTextPointF.y -= fixedDistanceY
      data.unselectedTextPointF.y -= fixedDistanceY
      data.selected = isSelected(data.rectF, centerX, centerY)
      if (data.selected) {
        selectedItemPosition = index
      }
    }

    onSelectionChangedListener?.onSelectionChanged(
      position = selectedItemPosition,
      selectedItem = items[selectedItemPosition].text
    )

    this.invalidate()
  }

  private fun animateDragY(distanceY: Float) {
    var prevValue = 0f
    val animation = ValueAnimator.ofFloat(0f, distanceY)
    animation.addUpdateListener {
      val value = it.animatedValue as Float
      val diff = value - prevValue
      prevValue = value
      computeScrollY(diff)
    }
    animation.interpolator = AccelerateDecelerateInterpolator()
    animation.duration = 250
    animation.start()
  }

  private fun animateDragY() {
    if (rectangles.isEmpty()) {
      return
    }
    val selectedItem = rectangles[selectedItemPosition]
    val distanceY = selectedItem.rectF.centerY() - centerY
    animateDragY(distanceY)
  }

  private fun animateFlingY(velocityY: Float) {
    val floatValueHolder = FloatValueHolder(scrollY)
    val animation = FlingAnimation(floatValueHolder)
    animation.setStartVelocity(velocityY)
    animation.setStartValue(scrollY)
    animation.setMaxValue(maxScrollY)
    animation.setMinValue(0f)
    animation.friction = 15f
    animation.addUpdateListener { _, value, _ ->
      val distanceY = scrollY - value
      computeScrollY(distanceY)
    }
    animation.addEndListener { _, _, _, _ ->
      animateDragY()
    }
    animation.start()
  }

  private fun isSelected(bounds: RectF, x: Float, y: Float): Boolean {
    return bounds.contains(x, y)
  }

  private fun log(message: String) {
    Log.d(TAG, message)
  }

  interface OnSelectionChangedListener {
    fun onSelectionChanged(position: Int, selectedItem: String)
  }

  private data class Item(
    val text: String
  )

  private data class ViewParams(
    var width: Int,
    var height: Int,
    var rowHeight: Float = 0f,
    var rowWidth: Float = 0f
  )

  private data class ItemData(
    val rectF: RectF,
    val item: Item,
    val selectedTextPointF: PointF,
    val unselectedTextPointF: PointF,
    var selected: Boolean = false
  )

  private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
    override fun onDown(e: MotionEvent): Boolean {
      return true
    }

    override fun onScroll(
      e1: MotionEvent?,
      e2: MotionEvent,
      distanceX: Float,
      distanceY: Float
    ): Boolean {
      computeScrollY(distanceY)
      return true
    }

    override fun onFling(
      e1: MotionEvent?,
      e2: MotionEvent,
      velocityX: Float,
      velocityY: Float
    ): Boolean {
      animateFlingY(velocityY)
      return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
      computeSingleTap(e.x, e.y)
      return true
    }
  }

  companion object {
    private const val TAG = "VerticalWheelSelector"
    private const val DEFAULT_NUM_ROWS = 3
    private const val DIVIDER_HEIGHT = 1f
  }
}
