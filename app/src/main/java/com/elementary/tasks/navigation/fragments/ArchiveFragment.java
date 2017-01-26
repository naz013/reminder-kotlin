package com.elementary.tasks.navigation.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.file_explorer.FilterCallback;
import com.elementary.tasks.core.interfaces.RealmCallback;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.DataLoader;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.FragmentTrashBinding;
import com.elementary.tasks.reminder.RecyclerListener;
import com.elementary.tasks.reminder.RemindersRecyclerAdapter;
import com.elementary.tasks.reminder.models.Reminder;

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

public class ArchiveFragment extends BaseNavigationFragment {

    private FragmentTrashBinding binding;
    private RecyclerView mRecyclerView;

    private RemindersRecyclerAdapter mAdapter;
    private List<Reminder> mDataList = new ArrayList<>();

    private SearchView mSearchView = null;
    private MenuItem mSearchMenu = null;

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (mAdapter != null) mAdapter.filter(query, mDataList);
            if (mSearchMenu != null) {
                mSearchMenu.collapseActionView();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (mAdapter != null) mAdapter.filter(newText, mDataList);
            return false;
        }
    };
    private FilterCallback mFilterCallback = new FilterCallback() {
        @Override
        public void filter(int size) {
            mRecyclerView.scrollToPosition(0);
            reloadView();
        }
    };
    private SearchView.OnCloseListener mSearchCloseListener = () -> false;
    private RecyclerListener mEventListener = new RecyclerListener() {
        @Override
        public void onItemSwitched(int position, View view) {

        }

        @Override
        public void onItemClicked(int position, View view) {
            Reminder reminder = mDataList.get(position);
            editReminder(reminder.getUuId());
        }

        @Override
        public void onItemLongClicked(int position, View view) {
            showActionDialog(position);
        }
    };
    private RealmCallback<List<Reminder>> mLoadCallback = this::showData;

    private void showData(List<Reminder> result) {
        mDataList = result;
        mAdapter = new RemindersRecyclerAdapter(mContext, mDataList, mFilterCallback);
        mAdapter.setEventListener(mEventListener);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        reloadView();
        getActivity().invalidateOptionsMenu();
    }

    private void showActionDialog(int position) {
        final String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(mContext, item -> {
            Reminder item1 = mAdapter.getItem(position);
            if (item == 0) {
                editReminder(item1.getUuId());
            }
            if (item == 1) {
                deleteReminder(item1);
                mAdapter.removeItem(position);
                Toast.makeText(mContext, R.string.deleted, Toast.LENGTH_SHORT).show();
                reloadView();
            }
        }, items);
    }

    private void deleteReminder(Reminder reminder) {
        RealmDb.getInstance().deleteReminder(reminder.getUuId());
        CalendarUtils.getInstance(mContext).deleteEvents(reminder.getUuId());
    }

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
        if (mDataList.size() == 0){
            menu.findItem(R.id.action_delete_all).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false);
        } else {
            menu.findItem(R.id.action_delete_all).setVisible(true);
            menu.findItem(R.id.action_search).setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all:
                deleteAll();
                loadData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editReminder(String uuId) {
        startActivity(new Intent(mContext, CreateReminderActivity.class).putExtra(Constants.INTENT_ID, uuId));
    }

    private void deleteAll(){
        for (Reminder reminder : mDataList) {
            deleteReminder(reminder);
        }
        Toast.makeText(mContext, getString(R.string.trash_cleared), Toast.LENGTH_SHORT).show();
        loadData();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTrashBinding.inflate(inflater, container, false);
        initList();
        return binding.getRoot();
    }

    private void initList() {
        mRecyclerView = binding.recyclerView;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.trash));
            mCallback.onFragmentSelect(this);
            mCallback.setClick(null);
        }
        loadData();
    }

    private void loadData() {
        DataLoader.loadArchivedReminder(mLoadCallback);
    }

    private void reloadView() {
        int size = mAdapter.getItemCount();
        if (size > 0){
            mRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyItem.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            binding.emptyItem.setVisibility(View.VISIBLE);
        }
    }
}
