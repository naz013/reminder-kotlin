package com.elementary.tasks.core.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Module;

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

public class ColorPickerView extends LinearLayout {

    private ImageButton red, green, blue, yellow, greenLight, blueLight, cyan, purple,
            amber, orange, pink, teal, deepPurple, deepOrange, indigo, lime;
    private int prevId;
    private int mSelectedCode;
    private OnColorListener mColorListener;
    private Context mContext;

    public ColorPickerView(Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        this.mContext = context;
        View.inflate(context, R.layout.color_picker_view_layout, this);
        setOrientation(VERTICAL);
        red = (ImageButton) findViewById(R.id.red_checkbox);
        purple = (ImageButton) findViewById(R.id.violet_checkbox);
        green = (ImageButton) findViewById(R.id.green_checkbox);
        greenLight = (ImageButton) findViewById(R.id.light_green_checkbox);
        blue = (ImageButton) findViewById(R.id.blue_checkbox);
        blueLight = (ImageButton) findViewById(R.id.light_blue_checkbox);
        yellow = (ImageButton) findViewById(R.id.yellow_checkbox);
        orange = (ImageButton) findViewById(R.id.orange_checkbox);
        cyan = (ImageButton) findViewById(R.id.grey_checkbox);
        pink = (ImageButton) findViewById(R.id.pink_checkbox);
        teal = (ImageButton) findViewById(R.id.sand_checkbox);
        amber = (ImageButton) findViewById(R.id.brown_checkbox);

        deepPurple = (ImageButton) findViewById(R.id.deepPurple);
        indigo = (ImageButton) findViewById(R.id.indigoCheckbox);
        lime = (ImageButton) findViewById(R.id.limeCheckbox);
        deepOrange = (ImageButton) findViewById(R.id.deepOrange);

        LinearLayout themeGroupPro = (LinearLayout) findViewById(R.id.themeGroupPro);
        if (Module.isPro()) {
            themeGroupPro.setVisibility(View.VISIBLE);
        } else themeGroupPro.setVisibility(View.GONE);

        setOnClickListener(red, green, blue, yellow, greenLight, blueLight, cyan, purple,
                amber, orange, pink, teal, deepPurple, deepOrange, indigo, lime);
    }

    public int getSelectedCode() {
        return mSelectedCode;
    }

    public void setSelectedColor(int code){
        switch (code) {
            case 0:
                red.setSelected(true);
                break;
            case 1:
                purple.setSelected(true);
                break;
            case 2:
                greenLight.setSelected(true);
                break;
            case 3:
                green.setSelected(true);
                break;
            case 4:
                blueLight.setSelected(true);
                break;
            case 5:
                blue.setSelected(true);
                break;
            case 6:
                yellow.setSelected(true);
                break;
            case 7:
                orange.setSelected(true);
                break;
            case 8:
                cyan.setSelected(true);
                break;
            case 9:
                pink.setSelected(true);
                break;
            case 10:
                teal.setSelected(true);
                break;
            case 11:
                amber.setSelected(true);
                break;
            default:
                if (Module.isPro()) {
                    switch (code) {
                        case 12:
                            deepPurple.setSelected(true);
                            break;
                        case 13:
                            deepOrange.setSelected(true);
                            break;
                        case 14:
                            lime.setSelected(true);
                            break;
                        case 15:
                            indigo.setSelected(true);
                            break;
                        default:
                            blue.setSelected(true);
                            break;
                    }
                }
                break;

        }
    }

    private void setOnClickListener(View... views){
        for (View view : views){
            view.setOnClickListener(listener);
        }
    }

    private OnClickListener listener = v -> themeColorSwitch(v.getId());

    public void setListener(OnColorListener listener) {
        this.mColorListener = listener;
    }

    private void themeColorSwitch(int radio){
        if (radio == prevId) return;
        prevId = radio;
        disableAll();
        setSelected(radio);
        switch (radio){
            case R.id.red_checkbox:
                selectColor(0);
                break;
            case R.id.violet_checkbox:
                selectColor(1);
                break;
            case R.id.light_green_checkbox:
                selectColor(2);
                break;
            case R.id.green_checkbox:
                selectColor(3);
                break;
            case R.id.light_blue_checkbox:
                selectColor(4);
                break;
            case R.id.blue_checkbox:
                selectColor(5);
                break;
            case R.id.yellow_checkbox:
                selectColor(6);
                break;
            case R.id.orange_checkbox:
                selectColor(7);
                break;
            case R.id.grey_checkbox:
                selectColor(8);
                break;
            case R.id.pink_checkbox:
                selectColor(9);
                break;
            case R.id.sand_checkbox:
                selectColor(10);
                break;
            case R.id.brown_checkbox:
                selectColor(11);
                break;
            case R.id.deepPurple:
                selectColor(12);
                break;
            case R.id.deepOrange:
                selectColor(13);
                break;
            case R.id.limeCheckbox:
                selectColor(14);
                break;
            case R.id.indigoCheckbox:
                selectColor(15);
                break;
        }
    }

    private void selectColor(int code) {
        this.mSelectedCode = code;
        if (mColorListener != null) {
            mColorListener.onColorSelect(code);
        }
    }

    private void setSelected(int radio) {
        findViewById(radio).setSelected(true);
    }

    private void disableAll() {
        red.setSelected(false);
        purple.setSelected(false);
        greenLight.setSelected(false);
        green.setSelected(false);
        blueLight.setSelected(false);
        blue.setSelected(false);
        yellow.setSelected(false);
        orange.setSelected(false);
        cyan.setSelected(false);
        pink.setSelected(false);
        teal.setSelected(false);
        amber.setSelected(false);
        deepOrange.setSelected(false);
        deepPurple.setSelected(false);
        lime.setSelected(false);
        indigo.setSelected(false);
    }

    public interface OnColorListener{
        void onColorSelect(int code);
    }
}
