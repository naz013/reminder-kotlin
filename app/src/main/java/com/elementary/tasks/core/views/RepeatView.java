package com.elementary.tasks.core.views;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Prefs;
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
public class RepeatView extends LinearLayout implements TextWatcher {

    private static final String TAG = "RepeatView";

    private int seconds = 0;
    private int minutes = 1;
    private int hours = 2;
    private int days = 3;
    private int weeks = 4;

    private LinearLayout mPredictionView;
    private RoboTextView mEventView;
    private RoboEditText mRepeatInput;
    @Nullable
    private OnRepeatListener mRepeatListener;
    @Nullable
    private InputMethodManager mImm;

    private boolean showPrediction = true;
    private int mState = days;
    private int mDay;
    private int mMonth;
    private int mYear;
    private int mHour;
    private int mMinute;
    private int mRepeatValue;

    @NonNull
    private DateTimeView.OnSelectListener mDateListener = new DateTimeView.OnSelectListener() {
        @Override
        public void onDateSelect(long mills, int dayOfMonth, int mon, int y) {
            mYear = y;
            mMonth = mon;
            mDay = dayOfMonth;
            updatePrediction(mRepeatValue);
        }

        @Override
        public void onTimeSelect(long mills, int hourOfDay, int min) {
            mHour = hourOfDay;
            mMinute = min;
            updatePrediction(mRepeatValue);
        }
    };
    @NonNull
    private TimerPickerView.TimerListener mTimeListener = time -> initDateTime(System.currentTimeMillis() + time);

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
        View.inflate(context, R.layout.view_repeat, this);
        setOrientation(VERTICAL);
        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        mRepeatInput = findViewById(R.id.repeatTitle);
        mEventView = findViewById(R.id.eventView);
        mPredictionView = findViewById(R.id.predictionView);
        Spinner mRepeatType = findViewById(R.id.repeatType);
        mRepeatType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setState(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mRepeatInput.addTextChangedListener(this);
        mRepeatInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (mImm == null) return;
            if (!hasFocus) {
                mImm.hideSoftInputFromWindow(mRepeatInput.getWindowToken(), 0);
            } else {
                mImm.showSoftInput(mRepeatInput, 0);
            }
        });
        mRepeatInput.setOnClickListener(v -> {
            if (mImm == null) return;
            if (!mImm.isActive(mRepeatInput)) {
                mImm.showSoftInput(mRepeatInput, 0);
            }
        });
        mRepeatValue = 0;
        mRepeatInput.setText(String.valueOf(mRepeatValue));
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
        mRepeatType.setSelection(mState);
        initDateTime(System.currentTimeMillis());
    }

    @NonNull
    public DateTimeView.OnSelectListener getEventListener() {
        return mDateListener;
    }

    @NonNull
    public TimerPickerView.TimerListener getTimerListener() {
        return mTimeListener;
    }

    public void initDateTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);
        mHour = cal.get(Calendar.HOUR_OF_DAY);
        mMinute = cal.get(Calendar.MINUTE);
        updatePrediction(mRepeatValue);
    }

    public void setDateTime(@Nullable String dateTime) {
        initDateTime(TimeUtil.getDateTimeFromGmt(dateTime));
    }

    private void updatePrediction(int progress) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);
        boolean is24 = Prefs.getInstance(getContext()).is24HourFormatEnabled();
        if (showPrediction) {
            mPredictionView.setVisibility(VISIBLE);
            mEventView.setText(TimeUtil.getFullDateTime(calendar.getTimeInMillis() + progress * getMultiplier(), is24, false));
        } else {
            mPredictionView.setVisibility(INVISIBLE);
        }
    }

    public void enablePrediction(boolean enable) {
        if (enable) {
            mPredictionView.setVisibility(VISIBLE);
        } else {
            mPredictionView.setVisibility(INVISIBLE);
        }
        this.showPrediction = enable;
    }

    private void setState(int state) {
        this.mState = state;
        updatePrediction(mRepeatValue);
    }

    public void setListener(@Nullable OnRepeatListener listener) {
        this.mRepeatListener = listener;
    }

    private void updateEditField() {
        mRepeatInput.setSelection(mRepeatInput.getText().length());
    }

    public void setRepeat(long mills) {
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

    private void setProgress(int i) {
        mRepeatValue = i;
        mRepeatInput.setText(String.valueOf(i));
        updateEditField();
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
        long rep = mRepeatValue * getMultiplier();
        LogUtil.d(TAG, "getRepeat: " + rep);
        return rep;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            mRepeatValue = Integer.parseInt(s.toString());
            if (mRepeatListener != null) mRepeatListener.onProgress(mRepeatValue);
        } catch (NumberFormatException e) {
            mRepeatInput.setText("0");
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface OnRepeatListener {
        void onProgress(int progress);
    }
}
