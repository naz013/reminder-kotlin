package com.elementary.tasks.core.views;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;

import java.util.Calendar;

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

public class DateTimeView extends RelativeLayout implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private RoboTextView date;
    private RoboTextView time;
    private int mHour;
    private int mMinute;
    private int mYear;
    private int mMonth;
    private int mDay;
    private boolean isSingleMode;
    private Context mContext;
    private AttributeSet attrs;
    private OnSelectListener mListener;

    private View.OnClickListener mDateClick = view -> selectDate();

    public DateTimeView(Context context) {
        super(context);
        init(context, null);
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DateTimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void setEventListener(OnSelectListener listener) {
        mListener = listener;
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) return;
        this.attrs = attrs;
        View.inflate(context, R.layout.date_time_view_layout, this);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        date = (RoboTextView) findViewById(R.id.dateField);
        time = (RoboTextView) findViewById(R.id.timeField);
        date.setOnClickListener(mDateClick);
        time.setOnClickListener(v -> selectTime());
        setIcons();
        this.mContext = context;
        updateDateTime(0);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        date.setOnLongClickListener(l);
        time.setOnLongClickListener(l);
    }

    private void setIcons() {
        if (ThemeUtil.getInstance(mContext).isDark()) {
            date.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar_white, 0, 0, 0);
            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_white_24dp, 0, 0, 0);
        } else {
            date.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0);
            time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time_black_24dp, 0, 0, 0);
        }
    }

    public void setSingleText(String text) {
        isSingleMode = text != null;
        if (!isSingleMode) {
            time.setVisibility(VISIBLE);
            date.setOnClickListener(mDateClick);
            updateDateTime(0);
        } else {
            date.setText(text);
            date.setOnClickListener(null);
            time.setVisibility(GONE);
        }
    }

    public long getDateTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);
        return calendar.getTimeInMillis();
    }

    public void setDateTime(long dateTime) {
        updateDateTime(dateTime);
    }

    public void setDateTime(String dateTime) {
        long mills = TimeUtil.getDateTimeFromGmt(dateTime);
        updateDateTime(mills);
    }

    private void updateDateTime(long mills){
        if (mills == 0) {
            mills = System.currentTimeMillis();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        updateTime(mills);
        updateDate(mills);
    }

    private void updateDate(long mills){
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mills);
        date.setText(TimeUtil.getDate(cal.getTime()));
        if (mListener != null) mListener.onDateSelect(mills, mDay, mMonth, mYear);
    }

    private void updateTime(long mills){
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mills);
        time.setText(TimeUtil.getTime(cal.getTime(), Prefs.getInstance(mContext).is24HourFormatEnabled()));
        if (mListener != null) mListener.onTimeSelect(mills, mHour, mMinute);
    }

    public void selectDate() {
        new DatePickerDialog(mContext, this, mYear, mMonth, mDay).show();
    }

    public void selectTime() {
        new TimePickerDialog(mContext, this, mHour, mMinute, Prefs.getInstance(mContext).is24HourFormatEnabled()).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.mYear = year;
        this.mMonth = monthOfYear;
        this.mDay = dayOfMonth;
        final Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth);
        updateDate(cal.getTimeInMillis());
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.mHour = hourOfDay;
        this.mMinute = minute;
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        updateTime(cal.getTimeInMillis());
    }

    public interface OnSelectListener{
        void onDateSelect(long mills, int day, int month, int year);
        void onTimeSelect(long mills, int hour, int minute);
    }
}
