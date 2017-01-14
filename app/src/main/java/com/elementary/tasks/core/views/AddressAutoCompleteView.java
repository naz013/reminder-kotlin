package com.elementary.tasks.core.views;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Address;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.elementary.tasks.core.async.GeocoderTask;
import com.elementary.tasks.core.utils.AssetsUtil;

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

public class AddressAutoCompleteView extends AutoCompleteTextView {

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
