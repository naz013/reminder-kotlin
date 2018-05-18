package com.elementary.tasks.core.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.DrawableRes;
import android.util.AttributeSet;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;

import androidx.appcompat.content.res.AppCompatResources;

/**
 * Copyright 2017 Nazar Suhovich
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

public class TextViewWithIcon extends RoboTextView {

    private static final String TAG = "TextViewWithIcon";

    public TextViewWithIcon(Context context) {
        super(context);
        init(context, null);
    }

    public TextViewWithIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TextViewWithIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setIcon(@DrawableRes int icon) {
        if (icon == 0) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            return;
        }
        Drawable drawableLeft;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawableLeft = getContext().getDrawable(icon);
        } else {
            drawableLeft = AppCompatResources.getDrawable(getContext(), icon);
        }
        setCompoundDrawablePadding(MeasureUtils.dp2px(getContext(), 16));
        setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
    }

    private void init(Context context, AttributeSet attrs) {
        setCompoundDrawablePadding(MeasureUtils.dp2px(getContext(), 16));
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TextViewWithIcon, 0, 0);
            try {
                Drawable drawableLeft = null;
                ThemeUtil themeUtil = ThemeUtil.getInstance(context);
                boolean isDark = themeUtil.isDark();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isDark) {
                        drawableLeft = a.getDrawable(R.styleable.TextViewWithIcon_tv_icon_light);
                    } else {
                        drawableLeft = a.getDrawable(R.styleable.TextViewWithIcon_tv_icon);
                    }
                } else {
                    int drawableLeftId = a.getResourceId(R.styleable.TextViewWithIcon_tv_icon, -1);
                    if (isDark) {
                        drawableLeftId = a.getResourceId(R.styleable.TextViewWithIcon_tv_icon_light, -1);
                    }
                    if (drawableLeftId != -1) {
                        drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId);
                    }
                }
                setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
            } catch (Exception e) {
                LogUtil.d(TAG, "There was an error loading attributes.");
            } finally {
                a.recycle();
            }
        }
    }
}
