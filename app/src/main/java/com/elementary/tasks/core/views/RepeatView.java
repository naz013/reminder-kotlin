package com.elementary.tasks.core.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboEditText;
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

public class RepeatView extends LinearLayout implements SeekBar.OnSeekBarChangeListener, TextWatcher {

    private static final String TAG = "RepeatView";

    private int seconds = 0;
    private int minutes = 1;
    private int hours = 2;
    private int days = 3;
    private int weeks = 4;

    private LinearLayout predictionView;
    private RoboTextView eventView;
    private RoboEditText repeatTitle;
    private SeekBar repeatViewSeek;
    private Context mContext;
    private OnRepeatListener listener;
    private InputMethodManager imm;

    private boolean showPrediction = true;
    private int mState = days;
    private int day;
    private int month;
    private int year;
    private int hour;
    private int minute;
    private int repeat;

    private DateTimeView.OnSelectListener mListener = new DateTimeView.OnSelectListener() {
        @Override
        public void onDateSelect(long mills, int dayOfMonth, int mon, int y) {
            year = y;
            month = mon;
            day = dayOfMonth;
            updatePrediction(repeatViewSeek.getProgress());
        }

        @Override
        public void onTimeSelect(long mills, int hourOfDay, int min) {
            hour = hourOfDay;
            minute = min;
            updatePrediction(repeatViewSeek.getProgress());
        }
    };
    private TimerPickerView.TimerListener mTimerListener = time -> initDateTime(System.currentTimeMillis() + time);

    public RepeatView(Context context) {
        super(context);
        init(context, null);
    }

    public RepeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RepeatView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) return;
        View.inflate(context, R.layout.repeat_view_layout, this);
        setOrientation(VERTICAL);
        repeatTitle = (RoboEditText) findViewById(R.id.repeatTitle);
        eventView = (RoboTextView) findViewById(R.id.eventView);
        predictionView = (LinearLayout) findViewById(R.id.predictionView);
        repeatViewSeek = (SeekBar) findViewById(R.id.repeatViewSeek);
        Spinner mRepeatType = (Spinner) findViewById(R.id.repeatType);
        mRepeatType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setState(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        repeatViewSeek.setOnSeekBarChangeListener(this);
        repeatTitle.addTextChangedListener(this);
        repeatTitle.setOnFocusChangeListener((v, hasFocus) -> {
            imm = (InputMethodManager) mContext.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!hasFocus) {
                imm.hideSoftInputFromWindow(repeatTitle.getWindowToken(), 0);
            } else {
                imm.showSoftInput(repeatTitle, 0);
            }
        });
        repeatTitle.setOnClickListener(v -> {
            imm = (InputMethodManager) mContext.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!imm.isActive(repeatTitle)){
                imm.showSoftInput(repeatTitle, 0);
            }
        });
        repeatViewSeek.setProgress(0);
        repeatTitle.setText(String.valueOf(0));
        ImageView iconView = (ImageView) findViewById(R.id.viewIcon);
        if (new ThemeUtil(context).isDark()) {
            iconView.setImageResource(R.drawable.ic_refresh_white);
        } else {
            iconView.setImageResource(R.drawable.ic_refresh);
        }
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RepeatView, 0, 0);
            try {
                mState = a.getInt(R.styleable.RepeatView_repeat_type, days);
            } catch (Exception e) {
                LogUtil.e(TAG, "There was an error loading attributes.", e);
            } finally {
                a.recycle();
            }
        }
        this.mContext = context;
        mRepeatType.setSelection(mState);
        initDateTime(System.currentTimeMillis());
    }

    public DateTimeView.OnSelectListener getEventListener() {
        return mListener;
    }

    public TimerPickerView.TimerListener getTimerListener() {
        return mTimerListener;
    }

    public void initDateTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        updatePrediction(repeatViewSeek.getProgress());
    }

    public void setDateTime(String dateTime) {
        initDateTime(TimeUtil.getDateTimeFromGmt(dateTime));
    }

    private void updatePrediction(int progress) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        boolean is24 = Prefs.getInstance(mContext).is24HourFormatEnabled();
        if (showPrediction) {
            predictionView.setVisibility(VISIBLE);
            eventView.setText(TimeUtil.getFullDateTime(calendar.getTimeInMillis() + progress * getMultiplier(), is24, false));
        } else {
            predictionView.setVisibility(INVISIBLE);
        }
    }

    public void enablePrediction(boolean enable) {
        if (enable) {
            predictionView.setVisibility(VISIBLE);
        } else {
            predictionView.setVisibility(INVISIBLE);
        }
        this.showPrediction = enable;
    }

    private void setState(int state) {
        this.mState = state;
        updatePrediction(repeatViewSeek.getProgress());
    }

    public void setListener(OnRepeatListener listener) {
        this.listener = listener;
    }

    public void setMax(int max){
        repeatViewSeek.setMax(max);
    }

    private void setProgress(int progress){
        this.repeat = progress;
        if (progress < repeatViewSeek.getMax()) {
            repeatViewSeek.setProgress(progress);
            updateEditField();
        }
        updatePrediction(progress);
    }

    private void updateEditField() {
        repeatTitle.setSelection(repeatTitle.getText().length());
    }

    public void setRepeat(long mills){
        if (mills == 0) {
            setProgress(0);
            return;
        }
        if (mills % (TimeCount.DAY * 7) == 0) {
            long progress = mills / (TimeCount.DAY * 7);
            setProgress((int) progress);
            setState(weeks);
        } else if (mills % TimeCount.DAY == 0) {
            long progress = mills / TimeCount.DAY;
            setProgress((int) progress);
            setState(days);
        } else if (mills % TimeCount.HOUR == 0) {
            long progress = mills / TimeCount.HOUR;
            setProgress((int) progress);
            setState(hours);
        } else if (mills % TimeCount.MINUTE == 0) {
            long progress = mills / TimeCount.MINUTE;
            setProgress((int) progress);
            setState(minutes);
        } else if (mills % TimeCount.SECOND == 0) {
            long progress = mills / TimeCount.SECOND;
            setProgress((int) progress);
            setState(seconds);
        }
    }

    private long getMultiplier() {
        if (mState == seconds) return TimeCount.SECOND;
        else if (mState == minutes) return TimeCount.MINUTE;
        else if (mState == hours) return TimeCount.HOUR;
        else if (mState == days) return TimeCount.DAY;
        else if (mState == weeks) return TimeCount.DAY * 7;
        return TimeCount.DAY;
    }

    public long getRepeat() {
        long rep = repeat * getMultiplier();
        LogUtil.d(TAG, "getRepeat: " + rep);
        return rep;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.repeat = progress;
        repeatTitle.setText(String.valueOf(progress));
        if (listener != null){
            listener.onProgress(progress);
        }
        updatePrediction(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            int res = Integer.parseInt(s.toString());
            if (listener != null) listener.onProgress(res);
            if (res < repeatViewSeek.getMax()) {
                setProgress(res);
            }
        } catch (NumberFormatException e){
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface OnRepeatListener {
        void onProgress(int progress);
    }
}
