package com.elementary.tasks.core.views.roboto;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.AssetsUtil;

import androidx.appcompat.widget.AppCompatButton;

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

public class RoboButton extends AppCompatButton {

    private Typeface mTypeface;

    public RoboButton(Context context) {
        super(context);
        init(null);
    }

    public RoboButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RoboButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RoboButton);
            int fontCode = a.getInt(R.styleable.RoboButton_button_font_style, -1);
            if (fontCode != -1) {
                mTypeface = AssetsUtil.getTypeface(getContext(), fontCode);
            } else {
                mTypeface = AssetsUtil.getDefaultTypeface(getContext());
            }
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
