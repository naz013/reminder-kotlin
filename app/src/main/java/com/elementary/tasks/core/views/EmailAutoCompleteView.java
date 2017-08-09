package com.elementary.tasks.core.views;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.EmailItemLayoutBinding;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
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

public class EmailAutoCompleteView extends AppCompatAutoCompleteTextView {

    private static final String TAG = "EmailAutoCompleteView";

    private Context mContext;
    private Typeface mTypeface;
    private List<EmailItem> mData = new ArrayList<>();
    private EmailAdapter adapter;

    private EmailCallback mLoadCallback = new EmailCallback() {
        @Override
        public void onLoadFinish(List<EmailItem> list) {
            mData = list;
            setAdapter(adapter = new EmailAdapter(mData));
        }
    };

    public EmailAutoCompleteView(Context context) {
        super(context);
        init(context, null);
    }

    public EmailAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EmailAutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        setOnItemClickListener((adapterView, view, i, l) -> setText(((EmailItem) adapter.getItem(i)).getEmail()));
        new LoadAsync(mLoadCallback).execute();
    }

    private void performTypeValue(String s) {
        if (adapter != null) adapter.getFilter().filter(s);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTypeface != null) {
            setTypeface(mTypeface);
        }
    }

    private class EmailAdapter extends BaseAdapter implements Filterable {

        private List<EmailItem> items = new ArrayList<>();
        private ValueFilter filter;

        public EmailAdapter(List<EmailItem> items) {
            this.items = items;
            getFilter();
        }

        public void setItems(List<EmailItem> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = EmailItemLayoutBinding.inflate(LayoutInflater.from(mContext), viewGroup, false).getRoot();
            }
            EmailItem item = items.get(i);
            RoboTextView nameView = view.findViewById(R.id.nameView);
            RoboTextView emailView = view.findViewById(R.id.emailView);
            nameView.setText(item.getName());
            emailView.setText(item.getEmail());
            return view;
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new ValueFilter();
            }
            return filter;
        }

        class ValueFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    List<EmailItem> filterList = new ArrayList<>();
                    for (int i = 0; i < mData.size(); i++) {
                        WeakReference<String> reference = new WeakReference<>((mData.get(i).getEmail() + mData.get(i).getName()).toLowerCase());
                        if (reference.get().contains(constraint.toString().toLowerCase())) {
                            filterList.add(mData.get(i));
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                } else {
                    results.count = mData.size();
                    results.values = mData;
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (adapter != null) {
                    adapter.setItems((List<EmailItem>) results.values);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class LoadAsync extends AsyncTask<Void, Void, List<EmailItem>> {
        private EmailCallback mCallback;

        public LoadAsync(EmailCallback mCallback) {
            this.mCallback = mCallback;
        }

        @Override
        protected List<EmailItem> doInBackground(Void... voids) {
            List<EmailItem> list = new ArrayList<>();
            HashSet<String> emlRecsHS = new HashSet<>();
            ContentResolver cr = mContext.getContentResolver();
            String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_ID,
                    ContactsContract.CommonDataKinds.Email.DATA,
                    ContactsContract.CommonDataKinds.Photo.CONTACT_ID};
            String order = "CASE WHEN "
                    + ContactsContract.Contacts.DISPLAY_NAME
                    + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                    + ContactsContract.Contacts.DISPLAY_NAME
                    + ", "
                    + ContactsContract.CommonDataKinds.Email.DATA
                    + " COLLATE NOCASE";
            String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
            Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
            if (cur != null && cur.moveToFirst()) {
                do {
                    String name = cur.getString(1);
                    String emlAddr = cur.getString(3);
                    if (emlRecsHS.add(emlAddr.toLowerCase())) {
                        list.add(new EmailItem(name, emlAddr));
                    }
                } while (cur.moveToNext());
                cur.close();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<EmailItem> list) {
            super.onPostExecute(list);
            if (mCallback != null) mCallback.onLoadFinish(list);
        }
    }

    private static class EmailItem {
        private String name;
        private String email;

        public EmailItem(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    interface EmailCallback {
        void onLoadFinish(List<EmailItem> list);
    }
}
