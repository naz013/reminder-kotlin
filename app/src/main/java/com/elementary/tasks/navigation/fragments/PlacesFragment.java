package com.elementary.tasks.navigation.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.adapter.FilterableAdapter;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.FragmentPlacesBinding;
import com.elementary.tasks.places.CreatePlaceActivity;
import com.elementary.tasks.places.PlaceItem;
import com.elementary.tasks.places.PlacesRecyclerAdapter;

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

public class PlacesFragment extends BaseNavigationFragment {

    private FragmentPlacesBinding binding;
    private PlacesRecyclerAdapter mAdapter;

    private SearchView mSearchView = null;
    private MenuItem mSearchMenu = null;

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (mAdapter != null) mAdapter.filter(query);
            if (mSearchMenu != null) {
                mSearchMenu.collapseActionView();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (mAdapter != null) mAdapter.filter(newText);
            return false;
        }
    };
    private SearchView.OnCloseListener mSearchCloseListener = () -> false;
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
    private FilterableAdapter.Filter<PlaceItem, String> mFilter = new FilterableAdapter.Filter<PlaceItem, String>() {
        @Override
        public boolean filter(PlaceItem placeItem, String query) {
            return placeItem.getTitle().toLowerCase().contains(query.toLowerCase());
        }

        @Override
        public void onFilterEnd(List<PlaceItem> list, int size, String query) {
            binding.recyclerView.smoothScrollToPosition(0);
            refreshView();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.archive_menu, menu);
        mSearchMenu = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (mSearchMenu != null) {
            mSearchView = (SearchView) mSearchMenu.getActionView();
        }
        if (mSearchView != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            mSearchView.setOnQueryTextListener(queryTextListener);
            mSearchView.setOnCloseListener(mSearchCloseListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                deleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAll() {

        refreshView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlacesBinding.inflate(inflater, container, false);
        initList();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.places));
            mCallback.onFragmentSelect(this);
            mCallback.setClick(view -> startActivity(new Intent(mContext, CreatePlaceActivity.class)));
            mCallback.onScrollChanged(binding.recyclerView);
        }
        showData();
    }

    private void editPlace(int position) {
        mContext.startActivity(new Intent(mContext, CreatePlaceActivity.class).putExtra(Constants.INTENT_ID, mAdapter.getItem(position).getKey()));
    }

    private void initList() {
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        refreshView();
    }

    private void showData() {
        mAdapter = new PlacesRecyclerAdapter(mContext, RealmDb.getInstance().getAllPlaces(), mEventListener, mFilter);
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
