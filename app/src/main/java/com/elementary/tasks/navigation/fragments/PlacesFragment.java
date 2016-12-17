package com.elementary.tasks.navigation.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.FragmentPlacesBinding;
import com.elementary.tasks.places.CreatePlaceActivity;
import com.elementary.tasks.places.PlacesRecyclerAdapter;

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

public class PlacesFragment extends BaseNavigationFragment {

    private FragmentPlacesBinding binding;
    private PlacesRecyclerAdapter mAdapter;
    private SimpleListener mEventListener = new SimpleListener() {
        @Override
        public void onItemClicked(int position, View view) {
            startActivity(new Intent(mContext, CreatePlaceActivity.class)
                    .putExtra(Constants.INTENT_ID, mAdapter.getItem(position).getKey()));
        }

        @Override
        public void onItemLongClicked(int position, View view) {
            final String[] items = {getString(R.string.edit), getString(R.string.delete)};
            Dialogues.showLCAM(mContext, item -> {
                if (item == 0) {
                    editPlace(position);
                } else if (item == 1) {
                    mAdapter.deleteItem(position);
                    refreshView();
                }
            }, items);
        }
    };

    private void editPlace(int position) {
        mContext.startActivity(new Intent(mContext, CreatePlaceActivity.class).putExtra(Constants.INTENT_ID, mAdapter.getItem(position).getKey()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlacesBinding.inflate(inflater, container, false);
        initList();
        return binding.getRoot();
    }

    private void initList() {
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        refreshView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.places));
            mCallback.onFragmentSelect(this);
            mCallback.setClick(view -> startActivity(new Intent(mContext, CreatePlaceActivity.class)));
        }
        showData();
    }

    private void showData() {
        mAdapter = new PlacesRecyclerAdapter(mContext, RealmDb.getInstance().getAllPlaces(), mEventListener);
        binding.recyclerView.setAdapter(mAdapter);
        refreshView();
    }

    private void refreshView() {
        if (mAdapter == null || mAdapter.getItemCount() == 0) {
            binding.emptyItem.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyItem.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
