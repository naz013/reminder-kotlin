package com.elementary.tasks.reminder.lists;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.view_models.reminders.ArchiveRemindersViewModel;
import com.elementary.tasks.core.views.FilterView;
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment;
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity;
import com.elementary.tasks.databinding.FragmentTrashBinding;
import com.elementary.tasks.reminder.lists.filters.FilterCallback;
import com.elementary.tasks.reminder.lists.filters.ReminderFilterController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

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
public class ArchiveFragment extends BaseNavigationFragment implements FilterCallback<Reminder> {

    private FragmentTrashBinding binding;
    private ArchiveRemindersViewModel viewModel;

    private RemindersRecyclerAdapter mAdapter = new RemindersRecyclerAdapter();

    private List<String> mGroupsIds = new ArrayList<>();
    private List<FilterView.Filter> filters = new ArrayList<>();
    @NonNull
    private ReminderFilterController filterController = new ReminderFilterController(this);

    private SearchView mSearchView = null;
    private MenuItem mSearchMenu = null;

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (mAdapter != null) filterController.setSearchValue(query);
            if (mSearchMenu != null) {
                mSearchMenu.collapseActionView();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (mAdapter != null) filterController.setSearchValue(newText);
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
            if (view.getId() == R.id.button_more) {
                showActionDialog(position, view);
            } else {
                Reminder reminder = mAdapter.getItem(position);
                if (reminder != null) editReminder(reminder.getUniqueId());
            }
        }

        @Override
        public void onItemLongClicked(int position, View view) {
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
            if (searchManager != null) {
                mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            }
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
                viewModel.deleteAll(mAdapter.getData());
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(ArchiveRemindersViewModel.class);
        viewModel.events.observe(this, reminders -> {
            if (reminders != null) {
                showData(reminders);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.trash));
            getCallback().onFragmentSelect(this);
            getCallback().setClick(null);
        }
    }

    private void editReminder(int id) {
        startActivity(new Intent(getContext(), CreateReminderActivity.class).putExtra(Constants.INTENT_ID, id));
    }

    private void showData(List<Reminder> result) {
        filterController.setOriginal(result);
        reloadView();
        refreshFilters();
    }

    private void showActionDialog(int position, View view) {
        final String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showPopup(getContext(), view, item -> {
            Reminder item1 = mAdapter.getItem(position);
            if (item1 == null) return;
            if (item == 0) {
                editReminder(item1.getUniqueId());
            }
            if (item == 1) {
                viewModel.deleteReminder(item1, true);
            }
        }, items);
    }

    private void refreshFilters() {
        filters.clear();
        if (viewModel.groups.getValue() != null) {
            addGroupFilter(viewModel.groups.getValue());
            addTypeFilter(filters);
        }
        if (getCallback().isFiltersVisible()) {
            showRemindersFilter();
        }
    }

    private void initList() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new RemindersRecyclerAdapter();
        mAdapter.setEventListener(mEventListener);
        mAdapter.setEditable(false);
        binding.recyclerView.setAdapter(mAdapter);
    }

    private void showRemindersFilter() {
        getCallback().addFilters(filters, true);
    }

    private void addTypeFilter(List<FilterView.Filter> filters) {
        List<Reminder> reminders = filterController.getOriginal();
        if (reminders.size() == 0) {
            return;
        }
        Set<Integer> types = new LinkedHashSet<>();
        for (Reminder reminder : reminders) {
            types.add(reminder.getType());
        }
        FilterView.Filter filter = new FilterView.Filter(new FilterView.FilterElementClick() {
            @Override
            public void onClick(View view, int id) {
                filterController.setTypeValue(id);
            }

            @Override
            public void onMultipleSelected(View view, List<Integer> ids) {

            }
        });
        filter.add(new FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true));
        ThemeUtil util = ThemeUtil.getInstance(getContext());
        for (Integer integer : types) {
            filter.add(new FilterView.FilterElement(util.getReminderIllustration(integer), ReminderUtils.getType(getContext(), integer), integer));
        }
        if (filter.size() != 0) {
            filters.add(filter);
        }
    }

    private void addGroupFilter(List<Group> groups) {
        mGroupsIds = new ArrayList<>();
        FilterView.Filter filter = new FilterView.Filter(new FilterView.FilterElementClick() {
            @Override
            public void onClick(View view, int id) {
                if (id == 0) {
                    filterController.setGroupValue(null);
                } else {
                    filterController.setGroupValue(mGroupsIds.get(id - 1));
                }
            }

            @Override
            public void onMultipleSelected(View view, List<Integer> ids) {
                List<String> groups = new ArrayList<>();
                for (Integer i : ids) groups.add(mGroupsIds.get(i - 1));
                filterController.setGroupValues(groups);
            }
        });
        filter.add(new FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true));
        ThemeUtil util = ThemeUtil.getInstance(getContext());
        for (int i = 0; i < groups.size(); i++) {
            Group item = groups.get(i);
            filter.add(new FilterView.FilterElement(util.getCategoryIndicator(item.getColor()), item.getTitle(), i + 1));
            mGroupsIds.add(item.getUuId());
        }
        filters.add(filter);
    }

    private void reloadView() {
        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyItem.setVisibility(View.GONE);
        } else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyItem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChanged(@NonNull List<Reminder> result) {
        mAdapter.setData(result);
        binding.recyclerView.smoothScrollToPosition(0);
        reloadView();
    }
}
