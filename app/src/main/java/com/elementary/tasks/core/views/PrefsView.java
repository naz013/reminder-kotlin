package com.elementary.tasks.core.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;

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
public class PrefsView extends RelativeLayout {

    private static final int CHECK = 0;
    private static final int SWITCH = 1;
    private static final int VIEW = 2;
    private static final int TEXT = 3;

    private CheckBox checkBox;
    private SwitchCompat switchView;
    private TextView title;
    private TextView detail;
    private TextView prefsValue;
    private View dividerTop, dividerBottom, prefsView;

    private boolean isChecked;
    private boolean isForPro;
    private int viewType = CHECK;

    private List<PrefsView> mDependencyViews = new ArrayList<>();
    private List<PrefsView> mReverseDependencyViews = new ArrayList<>();
    private List<OnCheckedListener> mOnCheckedListeners = new ArrayList<>();

    public PrefsView(Context context) {
        super(context);
        init(context, null);
    }

    public PrefsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PrefsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.view_prefs, this);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        title = findViewById(R.id.prefsPrimaryText);
        detail = findViewById(R.id.prefsSecondaryText);
        prefsValue = findViewById(R.id.prefsValue);
        checkBox = findViewById(R.id.prefsCheck);
        dividerTop = findViewById(R.id.dividerTop);
        dividerBottom = findViewById(R.id.dividerBottom);
        prefsView = findViewById(R.id.prefsView);
        switchView = findViewById(R.id.prefs_switch);
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.PrefsView, 0, 0);
            String titleText = "";
            String detailText = "";
            String valueText = "";
            boolean divTop = false;
            boolean divBottom = false;
            int res = 0;
            try {
                titleText = a.getString(R.styleable.PrefsView_prefs_primary_text);
                detailText = a.getString(R.styleable.PrefsView_prefs_secondary_text);
                valueText = a.getString(R.styleable.PrefsView_prefs_value_text);
                divTop = a.getBoolean(R.styleable.PrefsView_prefs_divider_top, false);
                divBottom = a.getBoolean(R.styleable.PrefsView_prefs_divider_bottom, false);
                isForPro = a.getBoolean(R.styleable.PrefsView_prefs_pro, false);
                viewType = a.getInt(R.styleable.PrefsView_prefs_type, CHECK);
                res = a.getInt(R.styleable.PrefsView_prefs_view_resource, 0);
            } catch (Exception e) {
                LogUtil.e("PrefsView", "There was an error loading attributes.", e);
            } finally {
                a.recycle();
            }
            setTitleText(titleText);
            setDetailText(detailText);
            setDividerTop(divTop);
            setDividerBottom(divBottom);
            setView();
            setValueText(valueText);
            setViewResource(res);
        }
        setChecked(isChecked());
        setVisible();
    }

    public void setOnCheckedListener(OnCheckedListener listener) {
        this.mOnCheckedListeners.add(listener);
    }

    public void setForPro(boolean forPro) {
        isForPro = forPro;
        setVisible();
    }

    public void setDependentView(PrefsView view) {
        if (view != null) {
            mDependencyViews.add(view);
            view.setOnCheckedListener(checked -> checkDependency());
        }
        checkDependency();
    }

    private void checkDependency() {
        boolean enable = true;
        for (PrefsView prefsView : mDependencyViews) {
            if (!prefsView.isChecked()) {
                enable = false;
                break;
            }
        }
        setEnabled(enable);
    }

    public void setReverseDependentView(PrefsView view) {
        if (view != null) {
            mReverseDependencyViews.add(view);
            view.setOnCheckedListener(checked -> checkReverseDependency());
        }
        checkReverseDependency();
    }

    private void checkReverseDependency() {
        boolean enable = true;
        for (PrefsView prefsView : mReverseDependencyViews) {
            if (prefsView.isChecked()) {
                enable = false;
                break;
            }
        }
        setEnabled(enable);
    }

    private void setVisible() {
        if (isForPro) {
            if (Module.isPro()) {
                setVisibility(VISIBLE);
            } else {
                setVisibility(GONE);
            }
        } else {
            setVisibility(VISIBLE);
        }
    }

    private void setView() {
        hideAll();
        switch (viewType) {
            case CHECK:
                checkBox.setVisibility(VISIBLE);
                break;
            case SWITCH:
                switchView.setVisibility(VISIBLE);
                break;
            case TEXT:
                prefsValue.setVisibility(VISIBLE);
                break;
            case VIEW:
                prefsView.setVisibility(VISIBLE);
                break;
        }
    }

    private void hideAll() {
        checkBox.setVisibility(GONE);
        switchView.setVisibility(GONE);
        prefsValue.setVisibility(GONE);
        prefsView.setVisibility(GONE);
    }

    public void setTitleText(String text) {
        title.setText(text);
    }

    public void setDetailText(String text) {
        if (text == null) {
            detail.setVisibility(GONE);
            return;
        }
        detail.setText(text);
        detail.setVisibility(VISIBLE);
    }

    public void setValue(int value) {
        prefsValue.setText(String.valueOf(value));
    }

    public void setValueText(String text) {
        prefsValue.setText(text);
    }

    public void setViewResource(@DrawableRes int resource) {
        if (resource != 0) {
            Drawable drawableTop;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableTop = getContext().getDrawable(resource);
            } else {
                drawableTop = AppCompatResources.getDrawable(getContext(), resource);
            }
            prefsView.setBackground(drawableTop);
        }
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
        if (viewType == CHECK) checkBox.setChecked(checked);
        else if (viewType == SWITCH) switchView.setChecked(checked);
        if (mOnCheckedListeners != null) {
            for (OnCheckedListener listener : mOnCheckedListeners) {
                listener.onCheckedChange(checked);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        switchView.setEnabled(enabled);
        checkBox.setEnabled(enabled);
        prefsView.setEnabled(enabled);
        prefsValue.setEnabled(enabled);
        detail.setEnabled(enabled);
        title.setEnabled(enabled);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setDividerTop(boolean divider) {
        if (divider) {
            dividerTop.setVisibility(VISIBLE);
        } else {
            dividerTop.setVisibility(GONE);
        }
    }

    public void setDividerBottom(boolean divider) {
        if (divider) {
            dividerBottom.setVisibility(VISIBLE);
        } else {
            dividerBottom.setVisibility(GONE);
        }
    }

    public interface OnCheckedListener {
        void onCheckedChange(boolean checked);
    }
}
