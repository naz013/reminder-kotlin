package com.elementary.tasks.core.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    private static final long LONG_CLICK_TIME = 1500;

    private int mYear;
    private int mMonth;
    private int mDay;
    private ArrayList<DateTime> mDateTimeList;

    private Context mContext;

    private Paint paint;
    private List<Rect> mCells;
    private int mWidth;
    private int mHeight;
    private int mDefaultColor;
    private int mFillColor;
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
        ThemeUtil themeUtil = ThemeUtil.getInstance(context);
        if (themeUtil.isDark()) {
            mDefaultColor = Color.WHITE;
        } else {
            mDefaultColor = Color.BLACK;
        }
        paint.setTypeface(AssetsUtil.getTypeface(context, 9));
        mFillColor = themeUtil.getColor(themeUtil.colorAccent());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
        setOnTouchListener(this);
    }

    public void setDateClick(OnDateClick dateClick) {
        this.mDateClick = dateClick;
    }

    public void setDateLongClick(OnDateLongClick dateLongClick) {
        this.mDateLongClick = dateLongClick;
    }

    public void setDate(int year, int month) {
        mDateTimeList = new ArrayList<>();
        mDateTimeList.clear();
        mMonth = month;
        mYear = year;
        DateTime firstDateOfMonth = new DateTime(mYear, month + 1, 1, 0, 0, 0, 0);
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
            DateTime nextDateTime = lastDateTime.plusDays(i);
            mDateTimeList.add(nextDateTime);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mWidth = getWidth();
        this.mHeight = getHeight();
        if (mCells == null) initCells();
        for (int i = 0; i < ROWS * COLS; i++) {
            Rect rect = mCells.get(i);
            if (mTouchPosition == i) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(mFillColor);
                paint.setAlpha(50);
                canvas.drawRect(rect, paint);
            }
            int color = mDefaultColor;
            DateTime dateTime = mDateTimeList.get(i);
            if (dateTime.getMonth() != mMonth + 1) {
                color = Color.GRAY;
            } else if (dateTime.getDay() == mDay) {
                color = Color.RED;
            }
            drawRectText(dateTime.getDay().toString(), canvas, rect, color);
        }
    }

    private void initCells() {
        Rect bounds = new Rect();
        getLocalVisibleRect(bounds);
        int cellWidth = mWidth / COLS;
        int cellHeight = mHeight / ROWS;
        mCells = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int top = i * cellHeight;
                int left = j * cellWidth;
                Rect tmp = new Rect(left, top, left + cellWidth, top + cellHeight);
                mCells.add(tmp);
            }
        }
    }

    private void drawRectText(String text, Canvas canvas, Rect r, int color) {
        paint.setTextSize(25);
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
            case MotionEvent.ACTION_HOVER_MOVE:
                performMove(motionEvent);
                break;
        }
        return true;
    }

    private void performAction(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        mLongClickHandler.removeCallbacks(mLongRunnable);
        if (mTouchRect != null && mTouchRect.contains(x, y)) {
            if (mDateClick != null) mDateClick.onClick(mDateTimeList.get(mTouchPosition));
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
        invalidate();
    }

    private void performTouch(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        for (int i = 0; i < ROWS * COLS; i++) {
            if (mCells.get(i).contains(x, y)) {
                mTouchPosition = i;
                mTouchRect = mCells.get(i);
                mLongClickHandler.postDelayed(mLongRunnable, LONG_CLICK_TIME);
                break;
            }
        }
        invalidate();
    }

    public interface OnDateClick {
        void onClick(DateTime dateTime);
    }

    public interface OnDateLongClick {
        void onLongClick(DateTime dateTime);
    }
}
