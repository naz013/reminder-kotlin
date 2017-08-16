package com.elementary.tasks.core.views.roboto;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.MeasureUtils;
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

public class RoboRadioButton extends AppCompatRadioButton {

    private Typeface mTypeface;

    public RoboRadioButton(Context context) {
        super(context);
        init(context, null);
    }

    public RoboRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoboRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RoboRadioButton);
            int fontCode = a.getInt(R.styleable.RoboRadioButton_radio_font_style, -1);
            if (fontCode != -1) {
                mTypeface = AssetsUtil.getTypeface(getContext(), fontCode);
            } else {
                mTypeface = AssetsUtil.getDefaultTypeface(getContext());
            }
            Drawable drawableLeft = null;
            Drawable drawableRight = null;
            Drawable drawableBottom = null;
            Drawable drawableTop = null;
            if (Module.isLollipop()) {
                drawableLeft = a.getDrawable(R.styleable.RoboRadioButton_drawableLeftCompat);
                drawableRight = a.getDrawable(R.styleable.RoboRadioButton_drawableRightCompat);
                drawableBottom = a.getDrawable(R.styleable.RoboRadioButton_drawableBottomCompat);
                drawableTop = a.getDrawable(R.styleable.RoboRadioButton_drawableTopCompat);
            } else {
                final int drawableLeftId = a.getResourceId(R.styleable.RoboRadioButton_drawableLeftCompat, -1);
                final int drawableRightId = a.getResourceId(R.styleable.RoboRadioButton_drawableRightCompat, -1);
                final int drawableBottomId = a.getResourceId(R.styleable.RoboRadioButton_drawableBottomCompat, -1);
                final int drawableTopId = a.getResourceId(R.styleable.RoboRadioButton_drawableTopCompat, -1);

                if (drawableLeftId != -1) {
                    drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId);
                }
                if (drawableRightId != -1) {
                    drawableRight = AppCompatResources.getDrawable(context, drawableRightId);
                }
                if (drawableBottomId != -1) {
                    drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId);
                }
                if (drawableTopId != -1) {
                    drawableTop = AppCompatResources.getDrawable(context, drawableTopId);
                }
            }
            setCompoundDrawablePadding(MeasureUtils.dp2px(context, 8));
            setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
            a.recycle();
        } else {
            mTypeface = AssetsUtil.getDefaultTypeface(getContext());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTypeface != null) {
            setTypeface(mTypeface);
        }
    }
}
