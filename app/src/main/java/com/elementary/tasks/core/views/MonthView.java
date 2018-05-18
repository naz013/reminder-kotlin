package com.elementary.tasks.core.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import androidx.annotation.IntRange;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.elementary.tasks.core.calendar.Events;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hirondelle.date4j.DateTime;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class MonthView extends View implements View.OnTouchListener {

    private static final String TAG = "MonthView";
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int GRID_R_C = 3;
    private static final long LONG_CLICK_TIME = 500;

    private int mYear;
    private int mMonth;
    private int currentYear;
    private int currentMonth;
    private int currentDay;
    private List<DateTime> mDateTimeList;
    private Map<DateTime, Events> eventsMap = new HashMap<>();

    private Context mContext;

    private Paint paint;
    private Paint circlePaint;
    private List<Rect> mCells;
    private Map<Rect, List<Rect>> circlesMap = new HashMap<>();
    private int mWidth;
    private int mHeight;
    private int mDefaultColor;
    private int mTodayColor;
    private int mTouchPosition = -1;
    private Rect mTouchRect;

    private Handler mLongClickHandler = new Handler();
    private OnDateClick mDateClick;
    private OnDateLongClick mDateLongClick;
    private Runnable mLongRunnable = new Runnable() {
        @Override
        public void run() {
            mLongClickHandler.removeCallbacks(mLongRunnable);
            if (mTouchRect != null && mDateLongClick != null) {
                mDateLongClick.onLongClick(mDateTimeList.get(mTouchPosition));
            }
            cancelTouch();
        }
    };

    public MonthView(Context context) {
        super(context);
        init(context);
    }

    public MonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.circlePaint = new Paint();
        this.circlePaint.setAntiAlias(true);
        ThemeUtil themeUtil = ThemeUtil.getInstance(context);
        if (themeUtil.isDark()) {
            mDefaultColor = Color.WHITE;
        } else {
            mDefaultColor = Color.BLACK;
        }
        mTodayColor = themeUtil.getColor(themeUtil.colorPrimary(Prefs.getInstance(context).getTodayColor()));
        paint.setTypeface(AssetsUtil.getTypeface(context, 7));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        currentYear = calendar.get(Calendar.YEAR);
        setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        setOnTouchListener(this);
    }

    public void setDateClick(OnDateClick dateClick) {
        this.mDateClick = dateClick;
    }

    public void setDateLongClick(OnDateLongClick dateLongClick) {
        this.mDateLongClick = dateLongClick;
    }

    public void setEventsMap(Map<DateTime, Events> eventsMap) {
        this.eventsMap = eventsMap;
    }

    public void setDate(int year, @IntRange(from = 1, to = 12) int month) {
        mDateTimeList = new ArrayList<>();
        mDateTimeList.clear();
        mMonth = month;
        mYear = year;
        DateTime firstDateOfMonth = new DateTime(mYear, mMonth, 1, 0, 0, 0, 0);
        DateTime lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.getNumDaysInMonth() - 1);
        int weekdayOfFirstDate = firstDateOfMonth.getWeekDay();
        int startDayOfWeek = Prefs.getInstance(mContext).getStartDay() + 1;
        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7;
        }
        while (weekdayOfFirstDate > 0) {
            DateTime dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate
                    - startDayOfWeek);
            if (!dateTime.lt(firstDateOfMonth)) {
                break;
            }
            mDateTimeList.add(dateTime);
            weekdayOfFirstDate--;
        }
        for (int i = 0; i < lastDateOfMonth.getDay(); i++) {
            mDateTimeList.add(firstDateOfMonth.plusDays(i));
        }
        int endDayOfWeek = startDayOfWeek - 1;
        if (endDayOfWeek == 0) {
            endDayOfWeek = 7;
        }
        if (lastDateOfMonth.getWeekDay() != endDayOfWeek) {
            int i = 1;
            while (true) {
                DateTime nextDay = lastDateOfMonth.plusDays(i);
                mDateTimeList.add(nextDay);
                i++;
                if (nextDay.getWeekDay() == endDayOfWeek) {
                    break;
                }
            }
        }
        int size = mDateTimeList.size();
        int numOfDays = 42 - size;
        DateTime lastDateTime = mDateTimeList.get(size - 1);
        for (int i = 1; i <= numOfDays; i++) {
            WeakReference<DateTime> nextDateTime = new WeakReference<>(lastDateTime.plusDays(i));
            mDateTimeList.add(nextDateTime.get());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long start = System.currentTimeMillis();
        this.mWidth = getWidth();
        this.mHeight = getHeight();
        if (mCells == null) {
            initCells();
        }
        for (int i = 0; i < ROWS * COLS; i++) {
            Rect rect = mCells.get(i);
            int color;
            DateTime dateTime = mDateTimeList.get(i);
            if (mYear != dateTime.getYear() || mMonth != dateTime.getMonth()) {
                color = Color.GRAY;
            } else {
                if (eventsMap.containsKey(dateTime)) {
                    drawEvents(canvas, eventsMap.get(dateTime), rect);
                }
                if (dateTime.getDay() == currentDay && dateTime.getMonth() == currentMonth && dateTime.getYear() == currentYear) {
                    color = mTodayColor;
                } else {
                    color = mDefaultColor;
                }
            }
            drawRectText(dateTime.getDay().toString(), canvas, rect, color);
        }
        LogUtil.d(TAG, "onDraw: " + (System.currentTimeMillis() - start));
    }

    private void drawEvents(Canvas canvas, Events events, Rect rect) {
        List<Rect> rects = circlesMap.get(rect);
        int index = 0;
        events.moveToStart();
        circlePaint.setAlpha(50);
        circlePaint.setStyle(Paint.Style.FILL);
        int maxEvents = GRID_R_C * GRID_R_C;
        while (events.hasNext() && index < maxEvents) {
            WeakReference<Events.Event> event = new WeakReference<>(events.getNext());
            circlePaint.setColor(event.get().getColor());
            Rect r = rects.get(index);
            int cX = r.centerX();
            int cY = r.centerY();
            if (index > 0 && index < maxEvents - 1) {
                WeakReference<Events.Event> prev = new WeakReference<>(events.getPreviousWithoutMoving());
                if (prev.get() != null) {
                    Rect end = rects.get(index - 1);
                    canvas.drawLine(cX, cY, end.centerX(), end.centerY(), circlePaint);
                }
            }
            canvas.drawCircle(r.centerX(), r.centerY(), r.width() / 4f, circlePaint);
            index++;
        }
    }

    private void initCells() {
        WeakReference<Rect> bounds = new WeakReference<>(new Rect());
        getLocalVisibleRect(bounds.get());
        int cellWidth = mWidth / COLS;
        int cellHeight = mHeight / ROWS;
        mCells = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int top = i * cellHeight;
                int left = j * cellWidth;
                Rect tmp = new Rect(left, top, left + cellWidth, top + cellHeight);
                mCells.add(tmp);
                generateCircles(tmp);
            }
        }
    }

    private void generateCircles(Rect rect) {
        int circleWidth = rect.width() / GRID_R_C;
        int circleHeight = rect.height() / GRID_R_C;
        int rectTop = rect.top;
        int rectLeft = rect.left;
        List<Rect> rects = new ArrayList<>();
        for (int i = 0; i < GRID_R_C; i++) {
            for (int j = 0; j < GRID_R_C; j++) {
                int top = i * circleHeight + rectTop;
                int left = j * circleWidth + rectLeft;
                WeakReference<Rect> tmp = new WeakReference<>(new Rect(left, top, left + circleWidth, top + circleHeight));
                rects.add(tmp.get());
            }
        }
        circlesMap.put(rect, rects);
    }

    private void drawRectText(String text, Canvas canvas, Rect r, int color) {
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAlpha(100);
        paint.setColor(color);
        int width = r.width();
        int numOfChars = paint.breakText(text, true, width, null);
        int start = (text.length() - numOfChars) / 2;
        canvas.drawText(text, start, start + numOfChars, r.exactCenterX(), r.exactCenterY(), paint);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performTouch(motionEvent);
                break;
            case MotionEvent.ACTION_UP:
                performAction(motionEvent);
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelTouch();
                break;
            case MotionEvent.ACTION_MOVE:
                performMove(motionEvent);
                break;
        }
        return true;
    }

    private void performAction(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        mLongClickHandler.removeCallbacks(mLongRunnable);
        if (mTouchRect != null && mTouchRect.contains(x, y) && mDateClick != null) {
            mDateClick.onClick(mDateTimeList.get(mTouchPosition));
        }
        cancelTouch();
    }

    private void performMove(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        if (mTouchRect != null && !mTouchRect.contains(x, y)) {
            cancelTouch();
        }
    }

    private void cancelTouch() {
        mTouchPosition = -1;
        mTouchRect = null;
    }

    private void performTouch(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        for (int i = 0; i < ROWS * COLS; i++) {
            Rect rect = mCells.get(i);
            if (rect != null && rect.contains(x, y)) {
                mTouchPosition = i;
                mTouchRect = rect;
                mLongClickHandler.postDelayed(mLongRunnable, LONG_CLICK_TIME);
                break;
            }
        }
    }

    public interface OnDateClick {
        void onClick(DateTime dateTime);
    }

    public interface OnDateLongClick {
        void onLongClick(DateTime dateTime);
    }
}
