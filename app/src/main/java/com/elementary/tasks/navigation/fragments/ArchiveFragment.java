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
import com.elementary.tasks.core.adapter.FilterableAdapter;
import com.elementary.tasks.core.interfaces.RealmCallback;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.DataLoader;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.views.FilterView;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.FragmentTrashBinding;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.reminder.RecyclerListener;
import com.elementary.tasks.reminder.RemindersRecyclerAdapter;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    private ArrayList<String> mGroupsIds;
    private String mLastGroupId;
    private int lastType;

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
            if (!getCallback().isFiltersVisible()) {
                showRemindersFilter();
            }
            return false;
        }
    };
    private SearchView.OnCloseListener mSearchCloseListener = () -> {
        refreshFilters();
        return false;
    };
    private RecyclerListener mEventListener = new RecyclerListener() {
        @Override
        public void onItemSwitched(int position, View view) {

        }

        @Override
        public void onItemClicked(int position, View view) {
            Reminder reminder = mAdapter.getItem(position);
            editReminder(reminder.getUuId());
        }

        @Override
        public void onItemLongClicked(int position, View view) {
            showActionDialog(position);
        }
    };
    private RealmCallback<List<Reminder>> mLoadCallback = this::showData;
    private FilterableAdapter.Filter<Reminder, String> mFilter = new FilterableAdapter.Filter<Reminder, String>() {
        @Override
        public boolean filter(Reminder reminder, String query) {
            return reminder.getSummary().toLowerCase().contains(query.toLowerCase());
        }

        @Override
        public void onFilterEnd(List<Reminder> list, int size, String query) {
            mRecyclerView.smoothScrollToPosition(0);
            reloadView();
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
            mSearchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    if (!getCallback().isFiltersVisible()) {
                        showRemindersFilter();
                    }
                }
            });
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTrashBinding.inflate(inflater, container, false);
        initList();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.trash));
            getCallback().onFragmentSelect(this);
            getCallback().setClick(null);
        }
        loadData(mLastGroupId, lastType);
    }

    private void editReminder(String uuId) {
        startActivity(new Intent(getContext(), CreateReminderActivity.class).putExtra(Constants.INTENT_ID, uuId));
    }

    private void deleteAll(){
        RealmDb.getInstance().clearReminderTrash();
        loadData(mLastGroupId, lastType);
        Toast.makeText(getContext(), getString(R.string.trash_cleared), Toast.LENGTH_SHORT).show();
        reloadView();
    }

    private void showData(List<Reminder> result) {
        mAdapter = new RemindersRecyclerAdapter(getContext(), result, mFilter);
        mAdapter.setEventListener(mEventListener);
        mAdapter.setEditable(false);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        reloadView();
    }

    private void showActionDialog(int position) {
        final String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(getContext(), item -> {
            Reminder item1 = mAdapter.getItem(position);
            if (item == 0) {
                editReminder(item1.getUuId());
            }
            if (item == 1) {
                deleteReminder(item1);
                mAdapter.removeItem(position);
                Toast.makeText(getContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                reloadView();
            }
        }, items);
    }

    private void deleteReminder(Reminder reminder) {
        RealmDb.getInstance().deleteReminder(reminder.getUuId());
        CalendarUtils.deleteEvents(getContext(), reminder.getUuId());
        refreshFilters();
    }

    private void refreshFilters() {
        if (getCallback().isFiltersVisible()) {
            showRemindersFilter();
        }
    }

    private void initList() {
        mRecyclerView = binding.recyclerView;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void loadData(String groupId, int type) {
        mLastGroupId = groupId;
        lastType = type;
        if (groupId != null || type != 0) {
            DataLoader.loadArchivedReminder(groupId, type, mLoadCallback);
        } else {
            DataLoader.loadArchivedReminder(mLoadCallback);
        }
    }

    private void showRemindersFilter() {
        List<FilterView.Filter> filters = new ArrayList<>();
        addGroupFilter(filters);
        addTypeFilter(filters);
        getCallback().addFilters(filters, true);
    }

    private void addTypeFilter(List<FilterView.Filter> filters) {
        List<Reminder> reminders = mAdapter.getUsedData();
        if (reminders.size() == 0) {
            return;
        }
        Set<Integer> types = new LinkedHashSet<>();
        for (Reminder reminder : reminders) {
            types.add(reminder.getType());
        }
        FilterView.Filter filter = new FilterView.Filter((view, id) -> loadData(mLastGroupId, id));
        filter.add(new FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0));
        ThemeUtil util = ThemeUtil.getInstance(getContext());
        for (Integer integer : types) {
            filter.add(new FilterView.FilterElement(util.getReminderIllustration(integer), ReminderUtils.getType(getContext(), integer), integer));
        }
        if (filter.size() != 0) {
            filters.add(filter);
        }
    }

    private void addGroupFilter(List<FilterView.Filter> filters) {
        mGroupsIds = new ArrayList<>();
        FilterView.Filter filter = new FilterView.Filter((view, id) -> {
            if (id == 0) {
                loadData(null, lastType);
            } else {
                String catId = mGroupsIds.get(id - 1);
                loadData(catId, lastType);
            }
        });
        filter.add(new FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0));
        List<GroupItem> groups = RealmDb.getInstance().getAllGroups();
        ThemeUtil util = ThemeUtil.getInstance(getContext());
        for (int i = 0; i < groups.size(); i++) {
            GroupItem item = groups.get(i);
            filter.add(new FilterView.FilterElement(util.getCategoryIndicator(item.getColor()), item.getTitle(), i + 1));
            mGroupsIds.add(item.getUuId());
        }
        filters.add(filter);
    }

    private void reloadView() {
        if (mAdapter != null && mAdapter.getItemCount() > 0){
            mRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyItem.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            binding.emptyItem.setVisibility(View.VISIBLE);
        }
    }
}
