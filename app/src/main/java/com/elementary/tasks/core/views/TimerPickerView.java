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

package com.elementary.tasks.core.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboButton;
import com.elementary.tasks.core.views.roboto.RoboTextView;

public class TimerPickerView extends LinearLayout implements View.OnClickListener {

    private RoboTextView hoursView, minutesView, secondsView;
    private ImageButton deleteButton;

    private String timeString = "000000";

    private Context mContext;
    private AttributeSet attrs;

    public TimerPickerView(Context context) {
        super(context);
        init(context, null);
    }

    public TimerPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TimerPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public long getTimerValue() {
        return SuperUtil.getAfterTime(timeString);
    }

    public void setTimerValue(long mills) {
        timeString = TimeUtil.generateAfterString(mills);
        updateTimeView();
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) return;
        this.attrs = attrs;
        setOrientation(VERTICAL);
        View.inflate(context, R.layout.timer_picker_view_layout, this);
        hoursView = (RoboTextView) findViewById(R.id.hoursView);
        minutesView = (RoboTextView) findViewById(R.id.minutesView);
        secondsView = (RoboTextView) findViewById(R.id.secondsView);
        deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        if (ThemeUtil.getInstance(context).isDark()) {
            deleteButton.setImageResource(R.drawable.ic_backspace_white);
        } else {
            deleteButton.setImageResource(R.drawable.ic_backspace);
        }
        deleteButton.setOnClickListener(v -> {
            timeString = timeString.substring(0, timeString.length() - 1);
            timeString = "0" + timeString;
            updateTimeView();
        });
        deleteButton.setOnLongClickListener(v -> {
            timeString = "000000";
            updateTimeView();
            return true;
        });
        initButtons();
        this.mContext = context;
        updateTimeView();
    }

    private void initButtons() {
        RoboButton b1 = (RoboButton) findViewById(R.id.b1);
        RoboButton b2 = (RoboButton) findViewById(R.id.b2);
        RoboButton b3 = (RoboButton) findViewById(R.id.b3);
        RoboButton b4 = (RoboButton) findViewById(R.id.b4);
        RoboButton b5 = (RoboButton) findViewById(R.id.b5);
        RoboButton b6 = (RoboButton) findViewById(R.id.b6);
        RoboButton b7 = (RoboButton) findViewById(R.id.b7);
        RoboButton b8 = (RoboButton) findViewById(R.id.b8);
        RoboButton b9 = (RoboButton) findViewById(R.id.b9);
        RoboButton b0 = (RoboButton) findViewById(R.id.b0);
        if (b1 != null) {
            b1.setId(Integer.valueOf(101));
            b2.setId(Integer.valueOf(102));
            b3.setId(Integer.valueOf(103));
            b4.setId(Integer.valueOf(104));
            b5.setId(Integer.valueOf(105));
            b6.setId(Integer.valueOf(106));
            b7.setId(Integer.valueOf(107));
            b8.setId(Integer.valueOf(108));
            b9.setId(Integer.valueOf(109));
            b0.setId(Integer.valueOf(100));
            b1.setOnClickListener(this);
            b2.setOnClickListener(this);
            b3.setOnClickListener(this);
            b4.setOnClickListener(this);
            b5.setOnClickListener(this);
            b6.setOnClickListener(this);
            b7.setOnClickListener(this);
            b8.setOnClickListener(this);
            b9.setOnClickListener(this);
            b0.setOnClickListener(this);
        }

    }

    private void updateTimeView() {
        if (timeString.matches("000000")) deleteButton.setEnabled(false);
        else deleteButton.setEnabled(true);
        if (timeString.length() == 6){
            String hours = timeString.substring(0, 2);
            String minutes = timeString.substring(2, 4);
            String seconds = timeString.substring(4, 6);
            hoursView.setText(hours);
            minutesView.setText(minutes);
            secondsView.setText(seconds);
        }
    }

    @Override
    public void onClick(View view) {
        int ids = view.getId();
        if (ids >= 100 && ids < 110){
            String charS = String.valueOf(timeString.charAt(0));
            if (charS.matches("0")){
                timeString = timeString.substring(1, timeString.length());
                timeString = timeString + String.valueOf(ids - 100);
                updateTimeView();
            }
        }
    }
}
