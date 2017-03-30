package com.elementary.tasks.core.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Build;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;

import com.elementary.tasks.R;
import com.elementary.tasks.core.async.GeocoderTask;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

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

public class AddressAutoCompleteView extends AppCompatAutoCompleteTextView {

    private static final String TAG = "AddressAutoCompleteView";

    private Context mContext;
    private Typeface mTypeface;
    private List<Address> foundPlaces;
    private ArrayAdapter<String> adapter;
    private List<String> namesList;

    private GeocoderTask task;

    private GeocoderTask.GeocoderListener mExecutionCallback = new GeocoderTask.GeocoderListener() {
        @Override
        public void onAddressReceived(List<Address> addresses) {
            foundPlaces = addresses;
            namesList = new ArrayList<>();
            namesList.clear();
            for (Address selected:addresses){
                String addressText = String.format("%s, %s%s",
                        selected.getMaxAddressLineIndex() > 0 ? selected.getAddressLine(0) : "",
                        selected.getMaxAddressLineIndex() > 1 ? selected.getAddressLine(1) + ", " : "",
                        selected.getCountryName());
                namesList.add(addressText);
            }
            adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, namesList);
            setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    };

    public AddressAutoCompleteView(Context context) {
        super(context);
        init(context, null);
    }

    public AddressAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AddressAutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        mTypeface = AssetsUtil.getDefaultTypeface(getContext());
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AddressAutoCompleteView, 0, 0);
            try {
                Drawable drawableLeft = null;
                ThemeUtil themeUtil = ThemeUtil.getInstance(context);
                boolean isDark = themeUtil.isDark();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isDark) {
                        drawableLeft = a.getDrawable(R.styleable.AddressAutoCompleteView_icon_light);
                    } else {
                        drawableLeft = a.getDrawable(R.styleable.AddressAutoCompleteView_icon_dark);
                    }
                } else {
                    int drawableLeftId = a.getResourceId(R.styleable.AddressAutoCompleteView_icon_dark, -1);
                    if (isDark) {
                        drawableLeftId = a.getResourceId(R.styleable.AddressAutoCompleteView_icon_light, -1);
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

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                performTypeValue(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        setSingleLine(true);
        setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        setOnEditorActionListener((textView, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEARCH)) {
                performTypeValue(getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    public Address getAddress(int position) {
        return foundPlaces.get(position);
    }

    private void performTypeValue(String s) {
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
        task = new GeocoderTask(mContext, mExecutionCallback);
        task.execute(s);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTypeface != null) {
            setTypeface(mTypeface);
        }
    }
}
