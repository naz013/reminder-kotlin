package com.elementary.tasks.core.views

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.calendar.Events
import com.elementary.tasks.core.utils.MeasureUtils
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import hirondelle.date4j.DateTime
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class MonthView : View, View.OnTouchListener {

    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var currentYear: Int = 0
    private var currentMonth: Int = 0
    private var currentDay: Int = 0
    private var mDateTimeList: MutableList<DateTime>? = null
    private var eventsMap: Map<DateTime, Events> = HashMap()

    private var mContext: Context? = null

    private lateinit var paint: Paint
    private lateinit var circlePaint: Paint
    private lateinit var borderPaint: Paint

    private lateinit var horizontalGradient: LinearGradient
    private lateinit var verticalGradient: LinearGradient
    private lateinit var gradientColors: IntArray

    private var mCells: MutableList<Rect>? = null
    private val circlesMap = HashMap<Rect, List<Rect>>()

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    private var mDefaultColor: Int = 0
    private var mTodayColor: Int = 0
    private var mTouchColor: Int = 0

    private var mTouchPosition = -1
    private var mTouchRect: Rect? = null

    private val mLongClickHandler = Handler()
    private var mDateClick: OnDateClick? = null
    private var mDateLongClick: OnDateLongClick? = null
    private val mLongRunnable = object : Runnable {
        override fun run() {
            mLongClickHandler.removeCallbacks(this)
            if (mTouchRect != null && mDateLongClick != null) {
                mDateLongClick?.onLongClick(mDateTimeList!![mTouchPosition])
            }
            cancelTouch()
        }
    }

    @Inject
    lateinit var themeUtil: ThemeUtil
    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        this.mContext = context
        val colorSecondary = ThemeUtil.getThemeSecondaryColor(context)
        this.mTouchColor = ThemeUtil.colorWithAlpha(colorSecondary, 12)
        this.gradientColors = intArrayOf(Color.TRANSPARENT, ThemeUtil.colorWithAlpha(colorSecondary, 90), Color.TRANSPARENT)

        this.borderPaint = Paint()
        this.borderPaint.style = Paint.Style.STROKE
        this.borderPaint.strokeWidth = MeasureUtils.dp2px(context, 1).toFloat()

        this.paint = Paint()
        this.paint.isAntiAlias = true
        this.circlePaint = Paint()
        this.circlePaint.isAntiAlias = true
        this.mDefaultColor = if (themeUtil.isDark) {
            Color.WHITE
        } else {
            Color.BLACK
        }
        this.mTodayColor = themeUtil.getColor(themeUtil.colorPrimary(prefs.todayColor))
        this.paint.typeface = ResourcesCompat.getFont(context, R.font.merriweathersans_regular)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        this.currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        this.currentMonth = calendar.get(Calendar.MONTH) + 1
        this.currentYear = calendar.get(Calendar.YEAR)
        setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
        setOnTouchListener(this)
    }

    fun setDateClick(dateClick: OnDateClick) {
        this.mDateClick = dateClick
    }

    fun setDateLongClick(dateLongClick: OnDateLongClick) {
        this.mDateLongClick = dateLongClick
    }

    fun setEventsMap(eventsMap: Map<DateTime, Events>) {
        this.eventsMap = eventsMap
        invalidate()
    }

    fun setDate(year: Int, @IntRange(from = 1, to = 12) month: Int) {
        mDateTimeList = ArrayList()
        mMonth = month
        mYear = year
        val firstDateOfMonth = DateTime(mYear, mMonth, 1, 0, 0, 0, 0)
        val lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.numDaysInMonth - 1)
        var weekdayOfFirstDate = firstDateOfMonth.weekDay
        val startDayOfWeek = prefs.startDay + 1
        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7
        }
        while (weekdayOfFirstDate > 0) {
            val dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate - startDayOfWeek)
            if (!dateTime.lt(firstDateOfMonth)) {
                break
            }
            mDateTimeList?.add(dateTime)
            weekdayOfFirstDate--
        }
        for (i in 0 until lastDateOfMonth.day) {
            mDateTimeList!!.add(firstDateOfMonth.plusDays(i))
        }
        var endDayOfWeek = startDayOfWeek - 1
        if (endDayOfWeek == 0) {
            endDayOfWeek = 7
        }
        if (lastDateOfMonth.weekDay != endDayOfWeek) {
            var i = 1
            while (true) {
                val nextDay = lastDateOfMonth.plusDays(i)
                mDateTimeList?.add(nextDay)
                i++
                if (nextDay.weekDay == endDayOfWeek) {
                    break
                }
            }
        }
        val size = mDateTimeList?.size ?: 0
        val numOfDays = 42 - size
        val lastDateTime = mDateTimeList?.get(size - 1) ?: return
        for (i in 1..numOfDays) {
            val nextDateTime = WeakReference(lastDateTime.plusDays(i))
            mDateTimeList?.add(nextDateTime.get()!!)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val start = System.currentTimeMillis()
        this.mWidth = width
        this.mHeight = height
        if (mCells == null) {
            initCells()
        }
        for (i in 0 until ROWS * COLS) {
            val rect = mCells!![i]
            val color: Int
            val dateTime = mDateTimeList!![i]
            color = if (mYear != dateTime.year || mMonth != dateTime.month) {
                Color.GRAY
            } else {
                if (eventsMap.containsKey(dateTime)) {
                    val events = eventsMap[dateTime]
                    if (events != null) {
                        drawEvents(canvas, events, rect)
                    }
                }
                if (dateTime.day == currentDay && dateTime.month == currentMonth && dateTime.year == currentYear) {
                    mTodayColor
                } else {
                    mDefaultColor
                }
            }
            drawRectText(dateTime.day.toString(), canvas, rect, color, i)
        }
        Timber.d("onDraw: ${(System.currentTimeMillis() - start)}")
    }

    private fun drawEvents(canvas: Canvas, events: Events, rect: Rect) {
        val rects = circlesMap[rect] ?: return
        var index = 0
        events.moveToStart()
        circlePaint.alpha = 50
        circlePaint.style = Paint.Style.FILL
        val maxEvents = GRID_R_C * GRID_R_C
        while (events.hasNext() && index < maxEvents) {
            val event = WeakReference(events.next)
            circlePaint.color = event.get()!!.color
            val r = rects[index]
            val cX = r.centerX()
            val cY = r.centerY()
            if (index > 0 && index < maxEvents - 1) {
                val prev = WeakReference<Events.Event>(events.previousWithoutMoving)
                if (prev.get() != null) {
                    val end = rects[index - 1]
                    canvas.drawLine(cX.toFloat(), cY.toFloat(), end.centerX().toFloat(), end.centerY().toFloat(), circlePaint)
                }
            }
            canvas.drawCircle(r.centerX().toFloat(), r.centerY().toFloat(), r.width() / 4f, circlePaint)
            index++
        }
    }

    private fun initCells() {
        val bounds = WeakReference(Rect())
        getLocalVisibleRect(bounds.get())
        val cellWidth = mWidth / COLS
        val cellHeight = mHeight / ROWS
        horizontalGradient = LinearGradient(0f, 0f, cellWidth.toFloat(), 0f,
                gradientColors, null, Shader.TileMode.MIRROR)
        verticalGradient = LinearGradient(0f, 0f, 0f, cellHeight.toFloat(),
                gradientColors, null, Shader.TileMode.MIRROR)
        mCells = ArrayList()
        for (i in 0 until ROWS) {
            for (j in 0 until COLS) {
                val top = i * cellHeight
                val left = j * cellWidth
                val tmp = Rect(left, top, left + cellWidth, top + cellHeight)
                mCells?.add(tmp)
                generateCircles(tmp)
            }
        }
    }

    private fun generateCircles(rect: Rect) {
        val circleWidth = rect.width() / GRID_R_C
        val circleHeight = rect.height() / GRID_R_C
        val rectTop = rect.top
        val rectLeft = rect.left
        val rects = ArrayList<Rect>()
        for (i in 0 until GRID_R_C) {
            for (j in 0 until GRID_R_C) {
                val top = i * circleHeight + rectTop
                val left = j * circleWidth + rectLeft
                val tmp = WeakReference(Rect(left, top, left + circleWidth, top + circleHeight))
                rects.add(tmp.get()!!)
            }
        }
        circlesMap[rect] = rects
    }

    private fun drawRectText(text: String, canvas: Canvas, r: Rect, color: Int, i: Int) {
        paint.textSize = MeasureUtils.dp2px(context, 16).toFloat()
        paint.textAlign = Paint.Align.CENTER
        paint.alpha = 100
        paint.color = color
        paint.style = Paint.Style.FILL_AND_STROKE
        val width = r.width()
        val numOfChars = paint.breakText(text, true, width.toFloat(), null)
        val start = (text.length - numOfChars) / 2
        canvas.drawText(text, start, start + numOfChars, r.exactCenterX(), r.exactCenterY(), paint)

        if (i == 0 || ((i - 6) % 7) != 0) {
            borderPaint.shader = verticalGradient
            canvas.drawLine(r.right.toFloat(), r.top.toFloat(),
                    r.right.toFloat(), r.bottom.toFloat(), borderPaint)
        }
        if (i <= 34) {
            borderPaint.shader = horizontalGradient
            canvas.drawLine(r.left.toFloat(), r.bottom.toFloat(),
                    r.right.toFloat(), r.bottom.toFloat(), borderPaint)
        }
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> performTouch(motionEvent)
            MotionEvent.ACTION_UP -> performAction(motionEvent)
            MotionEvent.ACTION_CANCEL -> cancelTouch()
            MotionEvent.ACTION_MOVE -> performMove(motionEvent)
        }
        return true
    }

    private fun performAction(motionEvent: MotionEvent) {
        val x = motionEvent.x.toInt()
        val y = motionEvent.y.toInt()
        mLongClickHandler.removeCallbacks(mLongRunnable)
        if (mTouchRect != null && mTouchRect?.contains(x, y) == true && mDateClick != null) {
            mDateClick?.onClick(mDateTimeList!![mTouchPosition])
        }
        cancelTouch()
    }

    private fun performMove(motionEvent: MotionEvent) {
        val x = motionEvent.x.toInt()
        val y = motionEvent.y.toInt()
        if (mTouchRect != null && mTouchRect?.contains(x, y) == false) {
            cancelTouch()
        }
    }

    private fun cancelTouch() {
        mTouchPosition = -1
        mTouchRect = null
    }

    private fun performTouch(motionEvent: MotionEvent) {
        val x = motionEvent.x.toInt()
        val y = motionEvent.y.toInt()
        for (i in 0 until ROWS * COLS) {
            val rect = mCells!![i]
            if (rect.contains(x, y)) {
                mTouchPosition = i
                mTouchRect = rect
                mLongClickHandler.postDelayed(mLongRunnable, LONG_CLICK_TIME)
                break
            }
        }
    }

    interface OnDateClick {
        fun onClick(dateTime: DateTime)
    }

    interface OnDateLongClick {
        fun onLongClick(dateTime: DateTime)
    }

    companion object {
        private const val ROWS = 6
        private const val COLS = 7
        private const val GRID_R_C = 3
        private const val LONG_CLICK_TIME: Long = 500
    }
}
