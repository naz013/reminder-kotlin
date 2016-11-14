package com.elementary.tasks.core.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.elementary.tasks.core.utils.Prefs;

import java.util.ArrayList;
import java.util.Calendar;

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

public class MonthView extends View {

    private static final String TAG = "MonthView";
    private static final int ROWS = 6;
    private static final int COLS = 7;

    private int mYear;
    private int mMonth;
    private ArrayList<DateTime> mDateTimeList;

    private Context mContext;

    private Paint paint;
    private Rect[][] mCells;
    private int mWidth;
    private int mHeight;

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
    }

    public void setDate(int year, int month) {
        mDateTimeList = new ArrayList<>();
        mDateTimeList.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
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
        Log.d(TAG, "onDraw: w " + mWidth + ", h " + mHeight);
        if (mCells == null) initCells();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Rect rect = mCells[i][j];
                canvas.drawRect(rect, paint);
            }
        }
    }

    private void initCells() {
        Rect bounds = new Rect();
        getLocalVisibleRect(bounds);
        int viewTop = bounds.top;
        int viewLeft = bounds.left;
        Log.d(TAG, "initCells: t " + viewTop + ", l " + viewLeft);
        int cellWidth = mWidth / COLS;
        int cellHeight = mHeight / ROWS;
        Log.d(TAG, "initCells: cw " + cellWidth + ", ch " + cellHeight);
        mCells = new Rect[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int top = i * cellHeight;
                int left = j * cellWidth;
                Rect tmp = new Rect(left, top, left + cellWidth, top + cellHeight);
                mCells[i][j] = tmp;
//                Log.d(TAG, "initCells: t " + tmp.top + ", l " + tmp.left + ", r " + tmp.right + ", b " + tmp.bottom);
            }
        }
    }
}
