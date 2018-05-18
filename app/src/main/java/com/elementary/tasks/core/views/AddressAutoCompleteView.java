package com.elementary.tasks.core.views;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Address;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.elementary.tasks.core.async.GeocoderTask;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.LogUtil;

import java.util.List;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

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

    private GeocoderTask task;
    private AddressAdapter mAdapter;
    private boolean isEnabled = true;

    private GeocoderTask.GeocoderListener mExecutionCallback = new GeocoderTask.GeocoderListener() {
        @Override
        public void onAddressReceived(List<Address> addresses) {
            LogUtil.d(TAG, "onAddressReceived: " + addresses);
            foundPlaces = addresses;
            mAdapter = new AddressAdapter(getContext(), android.R.layout.simple_list_item_2, addresses);
            setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
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
                if (isEnabled) performTypeValue(charSequence.toString());
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

    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        super.setOnItemClickListener((adapterView, view, i, l1) -> {
            if (mAdapter != null) {
                isEnabled = false;
                setText(mAdapter.getName(i));
                isEnabled = true;
            }
            if (l != null) l.onItemClick(adapterView, view, i, l1);
        });
    }

    private class AddressAdapter extends ArrayAdapter<Address> {

        AddressAdapter(@NonNull Context context, int resource, @NonNull List<Address> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, null, false);
            }
            TextView tv1 = v.findViewById(android.R.id.text1);
            TextView tv2 = v.findViewById(android.R.id.text2);
            Address address = getItem(position);
            if (address == null) return v;
            if (address.getAddressLine(0) != null) {
                tv1.setText(address.getAddressLine(0));
                tv2.setText(formName(address));
            } else {
                tv1.setText(formName(address));
                tv2.setText("");
            }
            return v;
        }

        public String getName(int position) {
            return formName(getItem(position));
        }

        private String formName(Address address) {
            StringBuilder sb = new StringBuilder();
            sb.append(address.getFeatureName());
            if (address.getAdminArea() != null) {
                sb.append(", ").append(address.getAdminArea());
            }
            if (address.getCountryName() != null) {
                sb.append(", ").append(address.getCountryName());
            }
            return sb.toString();
        }
    }
}
