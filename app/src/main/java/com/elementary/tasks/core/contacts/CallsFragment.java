package com.elementary.tasks.core.contacts;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.elementary.tasks.core.file_explorer.FilterCallback;
import com.elementary.tasks.core.file_explorer.RecyclerClickListener;
import com.elementary.tasks.databinding.FragmentCallsBinding;

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
public class CallsFragment extends Fragment implements CallsLogListener {

    private Context mContext;
    private NumberCallback mCallback;

    private List<CallsItem> mDataList;
    private CallsRecyclerAdapter mAdapter;

    private FragmentCallsBinding binding;
    private RecyclerView mRecyclerView;

    private RecyclerClickListener mClickListener = new RecyclerClickListener() {
        @Override
        public void onItemClick(int position) {
            if (position != -1) {
                String number = mAdapter.getItem(position).getNumber();
                String name = mAdapter.getItem(position).getName();
                if (mCallback != null) {
                    mCallback.onContactSelected(number, name);
                }
            }
        }
    };
    private FilterCallback mFilterCallback = new FilterCallback() {
        @Override
        public void filter(int size) {
            mRecyclerView.scrollToPosition(0);
            refreshView(size);
        }
    };

    public CallsFragment() {
    }

    public static CallsFragment newInstance() {
        return new CallsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = context;
        }
        if (mCallback == null) {
            try {
                mCallback = (NumberCallback) context;
            } catch (ClassCastException e) {
                throw new ClassCastException();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mCallback == null) {
            try {
                mCallback = (NumberCallback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCallsBinding.inflate(inflater, container, false);
        initSearchView();
        initRecyclerView();
        binding.refreshLayout.setRefreshing(true);
        binding.refreshLayout.setOnRefreshListener(this::loadData);
        loadData();
        return binding.getRoot();
    }

    private void loadData() {
        new CallsAsync(mContext, this).execute();
    }

    private void initRecyclerView() {
        mRecyclerView = binding.contactsList;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setHasFixedSize(true);
    }

    private void initSearchView() {
        binding.searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAdapter != null) mAdapter.filter(s.toString(), mDataList);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void refreshView(int count) {
        if (count > 0) {
            binding.emptyItem.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            binding.emptyItem.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.searchField.getWindowToken(), 0);
    }

    @Override
    public void onLoaded(List<CallsItem> list) {
        this.mDataList = list;
        binding.refreshLayout.setRefreshing(false);
        mAdapter = new CallsRecyclerAdapter(mContext, list, mClickListener, mFilterCallback);
        mRecyclerView.setAdapter(mAdapter);
        refreshView(mAdapter.getItemCount());
    }
}
