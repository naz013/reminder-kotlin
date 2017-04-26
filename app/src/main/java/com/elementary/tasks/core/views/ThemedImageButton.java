package com.elementary.tasks.core.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.ThemeUtil;

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

public class ThemedImageButton extends AppCompatImageButton {

    private static final String TAG = "ThemedImageButton";
    private AttributeSet mAttrs;
    private Context mContext;

    public ThemedImageButton(Context context) {
        super(context);
        init(context, null);
    }

    public ThemedImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ThemedImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mAttrs = attrs;
        this.mContext = context;
        restore();
    }

    public void restore() {
        if (mAttrs != null) {
            TypedArray a = mContext.getTheme().obtainStyledAttributes(mAttrs, R.styleable.ThemedImageButton, 0, 0);
            try {
                int icon;
                if (ThemeUtil.getInstance(mContext).isDark()) {
                    icon = a.getResourceId(R.styleable.ThemedImageButton_tb_icon_light, 0);
                } else {
                    icon = a.getResourceId(R.styleable.ThemedImageButton_tb_icon, 0);
                }
                setImageResource(icon);
                String message = a.getString(R.styleable.ThemedImageButton_tb_message);
                if (message != null) {
                    setOnLongClickListener(v -> showMessage(message));
                }
            } catch (Exception e) {
                LogUtil.d(TAG, "There was an error loading attributes.");
            } finally {
                a.recycle();
            }
        }
    }

    private boolean showMessage(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        return true;
    }
}
