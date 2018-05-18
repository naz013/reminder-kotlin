package com.elementary.tasks.core.views;

import android.content.Context;
import android.content.res.TypedArray;
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
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.views.roboto.RoboEditText;

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
public class BeforePickerView extends LinearLayout implements TextWatcher {

    private static final String TAG = "BeforePickerView";

    private int seconds = 0;
    private int minutes = 1;
    private int hours = 2;
    private int days = 3;
    private int weeks = 4;

    private RoboEditText mBeforeInput;
    @Nullable
    private InputMethodManager mImm;

    private int mState = minutes;
    private int mRepeatValue;

    public BeforePickerView(Context context) {
        super(context);
        init(context, null);
    }

    public BeforePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BeforePickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.view_remind_before, this);
        setOrientation(VERTICAL);
        mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        mBeforeInput = findViewById(R.id.before_value_view);
        Spinner beforeType = findViewById(R.id.before_type_view);
        beforeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setState(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mBeforeInput.addTextChangedListener(this);
        mBeforeInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (mImm == null) return;
            if (!hasFocus) {
                mImm.hideSoftInputFromWindow(mBeforeInput.getWindowToken(), 0);
            } else {
                mImm.showSoftInput(mBeforeInput, 0);
            }
        });
        mBeforeInput.setOnClickListener(v -> {
            if (mImm == null) return;
            if (!mImm.isActive(mBeforeInput)) {
                mImm.showSoftInput(mBeforeInput, 0);
            }
        });
        mRepeatValue = 0;
        mBeforeInput.setText(String.valueOf(mRepeatValue));
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BeforePickerView, 0, 0);
            try {
                mState = a.getInt(R.styleable.BeforePickerView_before_type, minutes);
            } catch (Exception e) {
                LogUtil.e(TAG, "There was an error loading attributes.", e);
            } finally {
                a.recycle();
            }
        }
        beforeType.setSelection(mState);
    }

    private void setState(int state) {
        this.mState = state;
    }

    private void updateEditField() {
        mBeforeInput.setSelection(mBeforeInput.getText().length());
    }

    public void setBefore(long mills) {
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
        mBeforeInput.setText(String.valueOf(i));
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

    public long getBeforeValue() {
        long rep = mRepeatValue * getMultiplier();
        LogUtil.d(TAG, "getBeforeValue: " + rep);
        return rep;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            mRepeatValue = Integer.parseInt(s.toString());
        } catch (NumberFormatException e) {
            mBeforeInput.setText("0");
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
